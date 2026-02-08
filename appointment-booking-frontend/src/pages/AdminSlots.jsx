import { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import DashboardHeader from '../components/DashboardHeader';
import { doctors, admin } from '../api/client';

export default function AdminSlots() {
  const [doctorList, setDoctorList] = useState([]);
  const [doctorId, setDoctorId] = useState('');
  const [slotDate, setSlotDate] = useState('');
  const [startTime, setStartTime] = useState('09:00');
  const [endTime, setEndTime] = useState('09:30');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    doctors.list().then(({ data }) => setDoctorList(data)).catch(() => setError('Failed to load doctors'));
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');
    setLoading(true);
    try {
      await admin.createSlot({
        doctorId: Number(doctorId),
        slotDate,
        startTime,
        endTime,
      });
      setMessage('Slot created successfully.');
      setSlotDate('');
      setStartTime('09:00');
      setEndTime('09:30');
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to create slot');
    } finally {
      setLoading(false);
    }
  };

  const today = new Date().toISOString().split('T')[0];

  return (
    <Layout>
      <DashboardHeader />
      <div className="p-4 sm:p-6 lg:p-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-6">Manage slots</h1>
        <p className="text-gray-500 mb-6">Fixed slots are auto-generated on startup (4–6 weekdays, 2–3 weekends per doctor). Use this form to add extra slots.</p>
        {message && <div className="mb-4 p-4 bg-green-50 text-green-700 rounded-lg">{message}</div>}
        {error && <div className="mb-4 p-4 bg-red-50 text-red-700 rounded-lg">{error}</div>}
        <div className="bg-white rounded-xl border border-gray-200 p-6 max-w-md">
          <form onSubmit={handleSubmit} className="space-y-4">
            <label className="block">
              <span className="text-sm font-medium text-gray-700">Doctor</span>
              <select
                value={doctorId}
                onChange={(e) => setDoctorId(e.target.value)}
                required
                className="mt-1 block w-full rounded-lg border border-gray-300 px-4 py-2 focus:ring-teal-500 focus:border-teal-500"
              >
                <option value="">Select doctor</option>
                {doctorList.map((d) => (
                  <option key={d.id} value={d.id}>{d.name} – {d.serviceName}</option>
                ))}
              </select>
            </label>
            <label className="block">
              <span className="text-sm font-medium text-gray-700">Date</span>
              <input
                type="date"
                value={slotDate}
                onChange={(e) => setSlotDate(e.target.value)}
                min={today}
                required
                className="mt-1 block w-full rounded-lg border border-gray-300 px-4 py-2 focus:ring-teal-500 focus:border-teal-500"
              />
            </label>
            <label className="block">
              <span className="text-sm font-medium text-gray-700">Start time</span>
              <input
                type="time"
                value={startTime}
                onChange={(e) => setStartTime(e.target.value)}
                required
                className="mt-1 block w-full rounded-lg border border-gray-300 px-4 py-2 focus:ring-teal-500 focus:border-teal-500"
              />
            </label>
            <label className="block">
              <span className="text-sm font-medium text-gray-700">End time</span>
              <input
                type="time"
                value={endTime}
                onChange={(e) => setEndTime(e.target.value)}
                required
                className="mt-1 block w-full rounded-lg border border-gray-300 px-4 py-2 focus:ring-teal-500 focus:border-teal-500"
              />
            </label>
            <button
              type="submit"
              disabled={loading}
              className="w-full py-2 px-4 bg-teal-600 text-white font-medium rounded-lg hover:bg-teal-700 disabled:opacity-50"
            >
              {loading ? 'Creating...' : 'Add slot'}
            </button>
          </form>
        </div>
      </div>
    </Layout>
  );
}
