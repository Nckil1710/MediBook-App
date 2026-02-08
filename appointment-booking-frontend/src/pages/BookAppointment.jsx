import { useState, useEffect, useMemo } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import DashboardHeader from '../components/DashboardHeader';
import { services, doctors, slots, appointments } from '../api/client';

const DAY_NAMES = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

function formatTime(t) {
  if (!t) return '';
  return String(t).slice(0, 5);
}

/** Format date as YYYY-MM-DD in local timezone (avoids UTC offset bugs) */
function toLocalDateStr(d) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

export default function BookAppointment() {
  const location = useLocation();
  const navigate = useNavigate();
  const rescheduleAppointment = location.state?.rescheduleAppointment;

  const [serviceList, setServiceList] = useState([]);
  const [doctorList, setDoctorList] = useState([]);
  const [selectedService, setSelectedService] = useState('');
  const [selectedDoctor, setSelectedDoctor] = useState('');
  const [currentMonth, setCurrentMonth] = useState(() => {
    const d = new Date();
    return { year: d.getFullYear(), month: d.getMonth() };
  });
  const [selectedDate, setSelectedDate] = useState(null);
  const [daySlots, setDaySlots] = useState([]);
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [loading, setLoading] = useState(false);
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const calendarDays = useMemo(() => {
    const { year, month } = currentMonth;
    const first = new Date(year, month, 1);
    const last = new Date(year, month + 1, 0);
    const startPad = first.getDay();
    const days = [];
    for (let i = 0; i < startPad; i++) days.push(null);
    for (let d = 1; d <= last.getDate(); d++) days.push(new Date(year, month, d));
    return days;
  }, [currentMonth]);

  useEffect(() => {
    services.list().then(({ data }) => setServiceList(data)).catch(() => setError('Failed to load services'));
  }, []);

  useEffect(() => {
    if (!selectedService) {
      setDoctorList([]);
      setSelectedDoctor('');
      return;
    }
    doctors.byService(Number(selectedService)).then(({ data }) => setDoctorList(data)).catch(() => setDoctorList([]));
    if (!rescheduleAppointment) setSelectedDoctor('');
  }, [selectedService, rescheduleAppointment]);

  useEffect(() => {
    if (!rescheduleAppointment?.serviceId || serviceList.length === 0 || selectedService) return;
    setSelectedService(String(rescheduleAppointment.serviceId));
  }, [rescheduleAppointment?.id, rescheduleAppointment?.serviceId, serviceList.length, selectedService]);

  useEffect(() => {
    if (!rescheduleAppointment?.doctorId || doctorList.length === 0 || selectedDoctor) return;
    setSelectedDoctor(String(rescheduleAppointment.doctorId));
  }, [rescheduleAppointment?.doctorId, doctorList.length, selectedDoctor]);

  useEffect(() => {
    if (!selectedDoctor || !selectedDate) {
      setDaySlots([]);
      setSelectedSlot(null);
      return;
    }
    setLoadingSlots(true);
    setDaySlots([]);
    setSelectedSlot(null);
    const doctorId = Number(selectedDoctor);
    slots
      .byDate(doctorId, selectedDate)
      .then(({ data }) => setDaySlots(data || []))
      .catch(() => setDaySlots([]))
      .finally(() => setLoadingSlots(false));
  }, [selectedDoctor, selectedDate]);

  const handlePrevMonth = () => {
    setCurrentMonth((prev) => (prev.month === 0 ? { year: prev.year - 1, month: 11 } : { year: prev.year, month: prev.month - 1 }));
    setSelectedDate(null);
    setSelectedSlot(null);
  };

  const handleNextMonth = () => {
    setCurrentMonth((prev) => (prev.month === 11 ? { year: prev.year + 1, month: 0 } : { year: prev.year, month: prev.month + 1 }));
    setSelectedDate(null);
    setSelectedSlot(null);
  };

  const handleDateClick = (d) => {
    const dateStr = toLocalDateStr(d);
    const today = toLocalDateStr(new Date());
    if (dateStr < today) return;
    setSelectedDate(dateStr);
    setSelectedSlot(null);
  };

  const handleBook = async (e) => {
    e.preventDefault();
    if (!selectedSlot) return;
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      if (rescheduleAppointment?.id) {
        await appointments.reschedule(rescheduleAppointment.id, { newSlotId: selectedSlot.id });
        setSuccess('Appointment rescheduled successfully.');
        navigate('/my-appointments', { replace: true });
      } else {
        await appointments.book({ slotId: selectedSlot.id });
        setSuccess('Appointment booked successfully.');
        setSelectedSlot(null);
      }
    } catch (err) {
      setError(err.response?.data?.error || (rescheduleAppointment?.id ? 'Reschedule failed' : 'Booking failed'));
    } finally {
      setLoading(false);
    }
  };

  const today = toLocalDateStr(new Date());
  const monthLabel = new Date(currentMonth.year, currentMonth.month).toLocaleString('default', { month: 'long', year: 'numeric' });

  return (
    <Layout>
      <DashboardHeader />
      <div className="p-4 sm:p-6 lg:p-8">
        <h1 className="text-xl sm:text-2xl font-bold text-gray-900 mb-4 sm:mb-6">
          {rescheduleAppointment?.id ? 'Reschedule: pick a new slot' : 'Book Appointment'}
        </h1>
        {error && <div className="mb-4 p-4 bg-red-50 text-red-700 rounded-lg">{error}</div>}
        {success && <div className="mb-4 p-4 bg-green-50 text-green-700 rounded-lg">{success}</div>}

        <div className="grid lg:grid-cols-3 gap-4 sm:gap-6 lg:gap-8">
          {/* Filters */}
          <div className="lg:col-span-1 space-y-4">
            <label className="block">
              <span className="text-sm font-medium text-gray-700">Service (Specialty)</span>
              <select
                value={selectedService}
                onChange={(e) => setSelectedService(e.target.value)}
                className="mt-1 block w-full rounded-lg border border-gray-300 px-4 py-2 focus:ring-teal-500 focus:border-teal-500"
              >
                <option value="">Select service</option>
                {serviceList.map((s) => (
                  <option key={s.id} value={s.id}>{s.name}</option>
                ))}
              </select>
            </label>
            <label className="block">
              <span className="text-sm font-medium text-gray-700">Doctor</span>
              <select
                value={selectedDoctor}
                onChange={(e) => setSelectedDoctor(e.target.value)}
                className="mt-1 block w-full rounded-lg border border-gray-300 px-4 py-2 focus:ring-teal-500 focus:border-teal-500"
              >
                <option value="">Select doctor</option>
                {doctorList.map((d) => (
                  <option key={d.id} value={d.id}>{d.name} {d.title ? `(${d.title})` : ''}</option>
                ))}
              </select>
            </label>
          </div>

          {/* Calendar + Slots */}
          <div className="lg:col-span-2 space-y-4 sm:space-y-6">
            {selectedDoctor && (
              <>
                <div className="bg-white rounded-xl border border-gray-200 p-3 sm:p-4">
                  <div className="flex items-center justify-between mb-4">
                    <button type="button" onClick={handlePrevMonth} className="p-2 hover:bg-gray-100 rounded-lg">←</button>
                    <span className="font-semibold text-gray-900">{monthLabel}</span>
                    <button type="button" onClick={handleNextMonth} className="p-2 hover:bg-gray-100 rounded-lg">→</button>
                  </div>
                  <div className="grid grid-cols-7 gap-0.5 sm:gap-1 text-center text-xs sm:text-sm">
                    {DAY_NAMES.map((d) => (
                      <div key={d} className="font-medium text-gray-500 py-1">{d}</div>
                    ))}
                    {calendarDays.map((d, i) => {
                      if (!d) return <div key={`empty-${i}`} />;
                      const dateStr = toLocalDateStr(d);
                      const isPast = dateStr < today;
                      const isSelected = selectedDate === dateStr;
                      return (
                        <button
                          key={dateStr}
                          type="button"
                          disabled={isPast}
                          onClick={() => handleDateClick(d)}
                          className={`py-1.5 sm:py-2 rounded-lg text-gray-700 transition-colors ${
                            isPast ? 'opacity-40 cursor-not-allowed' : 'cursor-pointer hover:bg-teal-100'
                          } ${isSelected ? 'bg-teal-600 text-white hover:bg-teal-700' : ''}`}
                        >
                          {d.getDate()}
                        </button>
                      );
                    })}
                  </div>
                </div>

                <div className="bg-white rounded-xl border border-gray-200 p-3 sm:p-4">
                  <h3 className="font-medium text-gray-900 mb-3 text-sm sm:text-base">
                    {selectedDate ? `Slots for ${selectedDate} (tap to select)` : 'Select a date in the calendar above to see slots'}
                  </h3>
                  {!selectedDate ? (
                    <p className="text-sm text-gray-500">Available slots for the selected day will appear here. Booked slots are grayed out.</p>
                  ) : loadingSlots ? (
                    <p className="text-gray-500">Loading slots...</p>
                  ) : (
                    <div className="flex flex-wrap gap-2">
                      {daySlots.length === 0 ? (
                        <p className="text-gray-500">No slots for this date. Try another day or doctor.</p>
                      ) : (
                        daySlots.map((slot) => {
                          const isBooked = slot.available === false;
                          const isPast = selectedDate < today || (selectedDate === today && formatTime(slot.startTime) <= new Date().toTimeString().slice(0, 5));
                          const disabled = isBooked || isPast;
                          return (
                            <button
                              key={slot.id}
                              type="button"
                              disabled={disabled}
                              onClick={() => !disabled && setSelectedSlot(slot)}
                              className={`px-3 py-2 sm:px-4 rounded-lg text-sm font-medium transition-colors touch-manipulation ${
                                selectedSlot?.id === slot.id
                                  ? 'bg-teal-600 text-white'
                                  : disabled
                                  ? 'bg-gray-200 text-gray-500 cursor-not-allowed'
                                  : 'bg-gray-100 text-gray-700 hover:bg-teal-100 hover:text-teal-800'
                              }`}
                              title={isBooked ? 'Booked' : undefined}
                            >
                              {formatTime(slot.startTime)} {isBooked ? '(booked)' : ''}
                            </button>
                          );
                        })
                      )}
                    </div>
                  )}
                </div>

                {selectedSlot && (
                  <form onSubmit={handleBook} className="bg-white rounded-xl border border-gray-200 p-3 sm:p-4 space-y-4">
                    <p className="text-sm text-gray-600">
                      Selected: <strong>{selectedSlot.serviceName}</strong> with <strong>{selectedSlot.doctorName}</strong> on {selectedSlot.slotDate} at {formatTime(selectedSlot.startTime)}
                    </p>
                    <button
                      type="submit"
                      disabled={loading}
                      className="w-full py-2 px-4 bg-green-500 text-white font-medium rounded-lg hover:bg-green-600 disabled:opacity-50"
                    >
                      {loading ? (rescheduleAppointment?.id ? 'Rescheduling...' : 'Booking...') : (rescheduleAppointment?.id ? 'Confirm Reschedule' : 'Confirm Book Appointment')}
                    </button>
                  </form>
                )}
              </>
            )}
            {!selectedDoctor && (
              <p className="text-gray-500">Select a service and doctor to see the calendar and available slots.</p>
            )}
          </div>
        </div>
      </div>
    </Layout>
  );
}
