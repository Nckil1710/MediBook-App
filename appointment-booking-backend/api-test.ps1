# Smart Appointment Booking - API Test Script (PowerShell)
# Run with: .\api-test.ps1
# Prerequisites: Backend running on http://localhost:8080, MySQL schema "appointment" exists

$base = "http://localhost:8080/api"
$token = $null

function Api {
    param([string]$Method, [string]$Path, [object]$Body = $null, [string]$AuthToken = $null)
    $uri = "$base$Path"
    $headers = @{ "Content-Type" = "application/json" }
    if ($AuthToken) { $headers["Authorization"] = "Bearer $AuthToken" }
    try {
        if ($Body) {
            $json = $Body | ConvertTo-Json -Depth 5
            return Invoke-RestMethod -Uri $uri -Method $Method -Headers $headers -Body $json
        } else {
            return Invoke-RestMethod -Uri $uri -Method $Method -Headers $headers
        }
    } catch {
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            Write-Host $reader.ReadToEnd() -ForegroundColor Red
        }
        return $null
    }
}

Write-Host "`n=== 1. Register new user ===" -ForegroundColor Cyan
$registerBody = @{ name = "Test User"; email = "testuser@example.com"; password = "test123"; phone = "9876543210" }
$reg = Api -Method POST -Path "/auth/register" -Body $registerBody
if ($reg) {
    $token = $reg.token
    Write-Host "Registered. Token received: $($token.Substring(0,20))..." -ForegroundColor Green
    Write-Host "UserId: $($reg.userId), Role: $($reg.role)"
} else { Write-Host "Register failed. Trying login with admin..." }

if (-not $token) {
    Write-Host "`n=== 1b. Login (admin) ===" -ForegroundColor Cyan
    $loginBody = @{ email = "admin@booking.com"; password = "admin123" }
    $login = Api -Method POST -Path "/auth/login" -Body $loginBody
    if ($login) {
        $token = $login.token
        Write-Host "Logged in as admin. Token received." -ForegroundColor Green
    }
}

if (-not $token) {
    Write-Host "No token. Exiting." -ForegroundColor Red
    exit 1
}

Write-Host "`n=== 2. GET /api/services ===" -ForegroundColor Cyan
$services = Api -Method GET -Path "/services" -AuthToken $token
if ($services) {
    $services | ForEach-Object { Write-Host "  Service: $($_.name) (id=$($_.id))" }
    $serviceId = $services[0].id
} else { $serviceId = 1 }

$tomorrow = (Get-Date).AddDays(1).ToString("yyyy-MM-dd")
Write-Host "`n=== 3. GET /api/slots/available (serviceId=$serviceId, date=$tomorrow) ===" -ForegroundColor Cyan
$slots = Api -Method GET -Path "/slots/available?serviceId=$serviceId&date=$tomorrow" -AuthToken $token
if ($slots -and $slots.Count -gt 0) {
    $slots | ForEach-Object { Write-Host "  Slot id=$($_.id) $($_.startTime)-$($_.endTime)" }
    $slotId = $slots[0].id
} else {
    Write-Host "  No slots. Admin must add slots first (step 6)." -ForegroundColor Yellow
    $slotId = $null
}

if ($slotId) {
    Write-Host "`n=== 4. POST /api/appointments/book ===" -ForegroundColor Cyan
    $bookBody = @{ slotId = $slotId; notes = "API test booking" }
    $book = Api -Method POST -Path "/appointments/book" -Body $bookBody -AuthToken $token
    if ($book) {
        Write-Host "  Booked appointment id=$($book.id), status=$($book.status)" -ForegroundColor Green
    }
}

Write-Host "`n=== 5. GET /api/appointments/my ===" -ForegroundColor Cyan
$my = Api -Method GET -Path "/appointments/my" -AuthToken $token
if ($my) {
    $my | ForEach-Object { Write-Host "  Appointment id=$($_.id) $($_.serviceName) $($_.slotDate) $($_.status)" }
} else { Write-Host "  None or error." }

# Admin endpoints (use admin token)
Write-Host "`n=== 6. Login as ADMIN and add a slot ===" -ForegroundColor Cyan
$adminLogin = Api -Method POST -Path "/auth/login" -Body (@{ email = "admin@booking.com"; password = "admin123" })
$adminToken = $adminLogin.token
if ($adminToken) {
    $slotBody = @{ serviceId = $serviceId; slotDate = $tomorrow; startTime = "09:00"; endTime = "09:30" }
    $newSlot = Api -Method POST -Path "/admin/slots" -Body $slotBody -AuthToken $adminToken
    if ($newSlot) { Write-Host "  Created slot id=$($newSlot.id)" -ForegroundColor Green }
    Write-Host "`n=== 7. GET /api/admin/appointments ===" -ForegroundColor Cyan
    $all = Api -Method GET -Path "/admin/appointments" -AuthToken $adminToken
    if ($all) { $all | ForEach-Object { Write-Host "  id=$($_.id) $($_.userName) $($_.serviceName) $($_.status)" } }
}

Write-Host "`n=== Done. Check MySQL for persistence (see API_TESTING.md). ===" -ForegroundColor Cyan
