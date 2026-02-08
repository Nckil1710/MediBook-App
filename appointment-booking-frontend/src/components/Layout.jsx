import { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const navItems = [
  { path: '/dashboard', label: 'Dashboard', icon: '📊' },
  { path: '/book', label: 'Book Appointment', icon: '📅' },
  { path: '/my-appointments', label: 'My Appointments', icon: '📋' },
  { path: '/profile', label: 'Profile', icon: '👤' },
];

export default function Layout({ children }) {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, isAdmin } = useAuth();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  useEffect(() => {
    const mq = window.matchMedia('(min-width: 1024px)');
    setSidebarOpen(mq.matches);
    const handler = () => setSidebarOpen(mq.matches);
    mq.addEventListener('change', handler);
    return () => mq.removeEventListener('change', handler);
  }, []);

  const handleSignOut = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
    window.location.reload();
  };

  return (
    <div className="flex min-h-screen w-full bg-gray-100 overflow-x-hidden">
      {/* Backdrop - mobile only, when sidebar open */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 z-10 bg-black/40 lg:hidden"
          onClick={() => setSidebarOpen(false)}
          aria-hidden="true"
        />
      )}
      {/* Sidebar - overlay on mobile (max 280px), inline on desktop */}
      <aside
        className={`fixed inset-y-0 left-0 z-20 lg:relative lg:inset-auto
          bg-slate-800 text-white flex flex-col shrink-0 min-h-screen
          transition-all duration-300 ease-in-out
          ${sidebarOpen ? 'w-[280px] max-w-[85vw] translate-x-0' : '-translate-x-full w-0 overflow-hidden'}
          lg:translate-x-0
          ${sidebarOpen ? 'lg:w-64' : 'lg:w-0 lg:overflow-hidden'}`}
      >
        <div className="w-[280px] max-w-[85vw] lg:w-64 flex flex-col h-full min-h-screen shrink-0">
          <div className="p-4 sm:p-6 border-b border-slate-700">
            <div className="flex items-center gap-2 min-w-0">
              <span className="text-xl sm:text-2xl shrink-0">❤️</span>
              <span className="font-semibold text-sm sm:text-lg truncate">MediBook Smart Appointments</span>
            </div>
          </div>
          <nav className="flex-1 p-4 space-y-1">
            {navItems.map(({ path, label, icon }) => (
              <Link
                key={path}
                to={path}
                onClick={() => window.innerWidth < 1024 && setSidebarOpen(false)}
                className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                  location.pathname === path
                    ? 'bg-teal-600 text-white'
                    : 'text-slate-300 hover:bg-slate-700 hover:text-white'
                }`}
              >
                <span>{icon}</span>
                <span>{label}</span>
              </Link>
            ))}
            {isAdmin && (
              <>
                <Link
                  to="/admin/slots"
                  onClick={() => window.innerWidth < 1024 && setSidebarOpen(false)}
                  className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                    location.pathname === '/admin/slots'
                      ? 'bg-teal-600 text-white'
                      : 'text-slate-300 hover:bg-slate-700 hover:text-white'
                  }`}
                >
                  <span>⏰</span>
                  <span>Manage Slots</span>
                </Link>
                <Link
                  to="/admin/appointments"
                  onClick={() => window.innerWidth < 1024 && setSidebarOpen(false)}
                  className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                    location.pathname === '/admin/appointments'
                      ? 'bg-teal-600 text-white'
                      : 'text-slate-300 hover:bg-slate-700 hover:text-white'
                  }`}
                >
                  <span>✓</span>
                  <span>Admin Appointments</span>
                </Link>
              </>
            )}
          </nav>
          <div className="p-4 border-t border-slate-700">
            <div className="flex items-center gap-3 px-3 py-2 mb-2">
              <div className="w-10 h-10 rounded-full bg-teal-500 flex items-center justify-center font-semibold text-white">
                {user?.name?.charAt(0)?.toUpperCase() || 'U'}
              </div>
              <div className="min-w-0">
                <p className="font-medium truncate">{user?.name}</p>
                <p className="text-xs text-slate-400">{isAdmin ? 'Admin' : 'Patient'}</p>
              </div>
            </div>
            <button
              type="button"
              onClick={handleSignOut}
              className="flex items-center gap-2 w-full px-4 py-2 text-slate-300 hover:bg-slate-700 rounded-lg transition-colors"
            >
              <span>→</span>
              <span>Sign Out</span>
            </button>
          </div>
        </div>
      </aside>
      {/* Main: top bar with hamburger, then page content */}
      <main className="flex-1 flex flex-col min-w-0 min-h-screen">
        <div className="flex items-center h-12 sm:h-14 px-3 sm:px-4 border-b border-gray-200 bg-white shrink-0 shadow-sm gap-2">
          <button
            type="button"
            onClick={() => setSidebarOpen((o) => !o)}
            className="p-2 -ml-1 rounded-lg text-gray-600 hover:bg-gray-100 transition-colors touch-manipulation"
            aria-label="Toggle sidebar"
          >
            <span className="text-xl">☰</span>
          </button>
        </div>
        <div className="flex-1 overflow-auto">
          {children}
        </div>
      </main>
    </div>
  );
}
