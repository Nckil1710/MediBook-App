import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Layout from '../components/Layout';
import DashboardHeader from '../components/DashboardHeader';
import { useAuth } from '../context/AuthContext';
import { appointments, slots, doctors } from '../api/client';

function formatTime(t) {
  if (!t) return '';
  return String(t).slice(0, 5);
}

function toLocalDateStr(d) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

function StatusBadge({ status }) {
  const styles = {
    PENDING: 'bg-amber-100 text-amber-800',
    APPROVED: 'bg-green-100 text-green-800',
    REJECTED: 'bg-red-100 text-red-800',
    COMPLETED: 'bg-blue-100 text-blue-800',
  };
  return (
    <span className={`px-3 py-1 rounded-full text-sm font-medium ${styles[status] || 'bg-gray-100 text-gray-800'}`}>
      {status}
    </span>
  );
}

export default function Dashboard() {
  const { user } = useAuth();
  const [appointmentList, setAppointmentList] = useState([]);
  const [availableCount, setAvailableCount] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    appointments.my().then(({ data }) => setAppointmentList(data)).catch(() => setAppointmentList([])).finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    const now = new Date();
    const today = toLocalDateStr(now);
    const nowHHMM = now.toTimeString().slice(0, 5);
    doctors.list()
      .then(({ data: docList }) => {
        if (docList.length === 0) return 0;
        return Promise.all(
          docList.map((d) =>
            slots.getAvailable(d.id, today).then((r) => {
              const list = Array.isArray(r.data) ? r.data : [];
              // Only count slots that are still bookable today (start time is in the future)
              return list.filter((s) => formatTime(s.startTime) > nowHHMM).length;
            })
          )
        );
      })
      .then((counts) => setAvailableCount(Array.isArray(counts) ? counts.reduce((a, b) => a + b, 0) : 0))
      .catch(() => setAvailableCount(0));
  }, []);

  const today = toLocalDateStr(new Date());
  const total = appointmentList.length;
  const upcoming = appointmentList.filter((a) => a.slotDate >= today && !['REJECTED'].includes(a.status)).length;
  const pendingApproval = appointmentList.filter((a) => a.status === 'PENDING').length;
  const completed = appointmentList.filter((a) => a.status === 'COMPLETED').length;

  const upcomingList = appointmentList
    .filter((a) => a.slotDate >= today && !['REJECTED'].includes(a.status))
    .sort((a, b) => new Date(a.slotDate + 'T' + a.startTime) - new Date(b.slotDate + 'T' + b.startTime))
    .slice(0, 5);

  return (
    <Layout>
      <DashboardHeader title="Dashboard" showBookButton />
      <div className="p-4 sm:p-6 lg:p-8">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Welcome back, {user?.name?.split(' ')[0] || user?.name} 👋</h1>
            <p className="text-gray-500 mt-1">Here&apos;s your appointment overview.</p>
          </div>
        </div>

        {/* Summary cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6 mb-6 sm:mb-8">
          <div className="bg-cyan-50 border border-cyan-100 rounded-xl p-4 sm:p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-cyan-700">Total Appointments</p>
                <p className="text-3xl font-bold text-cyan-900 mt-1">{total}</p>
              </div>
              <span className="text-4xl">📅</span>
            </div>
          </div>
          <div className="bg-green-50 border border-green-100 rounded-xl p-4 sm:p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-green-700">Upcoming</p>
                <p className="text-3xl font-bold text-green-900 mt-1">{upcoming}</p>
              </div>
              <span className="text-4xl">🕐</span>
            </div>
          </div>
          <div className="bg-amber-50 border border-amber-100 rounded-xl p-4 sm:p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-amber-700">Pending Approval</p>
                <p className="text-3xl font-bold text-amber-900 mt-1">{pendingApproval}</p>
              </div>
              <span className="text-4xl">📊</span>
            </div>
          </div>
          <div className="bg-blue-50 border border-blue-100 rounded-xl p-4 sm:p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-blue-700">Available Slots Today</p>
                <p className="text-3xl font-bold text-blue-900 mt-1">{availableCount}</p>
                <Link to="/book" className="text-sm font-medium text-blue-600 hover:underline mt-2 inline-block">Book now</Link>
              </div>
              <span className="text-4xl">📅</span>
            </div>
          </div>
        </div>

        {/* Upcoming Appointments table */}
        <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
          <h2 className="text-base sm:text-lg font-semibold text-gray-900 px-4 sm:px-6 py-3 sm:py-4 border-b border-gray-200">Upcoming Appointments</h2>
          {loading ? (
            <div className="p-8 text-center text-gray-500">Loading...</div>
          ) : upcomingList.length === 0 ? (
            <div className="p-8 text-center text-gray-500">No upcoming appointments. <Link to="/book" className="text-teal-600 hover:underline">Book one</Link>.</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full min-w-[500px]">
                <thead>
                  <tr className="bg-gray-50 text-left text-xs sm:text-sm font-medium text-gray-500">
                    <th className="px-4 sm:px-6 py-3">Doctor</th>
                    <th className="px-4 sm:px-6 py-3">Specialty</th>
                    <th className="px-4 sm:px-6 py-3">Date</th>
                    <th className="px-4 sm:px-6 py-3">Time</th>
                    <th className="px-4 sm:px-6 py-3">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {upcomingList.map((a) => (
                    <tr key={a.id} className="hover:bg-gray-50">
                      <td className="px-4 sm:px-6 py-3 sm:py-4 font-medium text-gray-900 text-sm">{a.doctorName || '—'}</td>
                      <td className="px-4 sm:px-6 py-3 sm:py-4 text-gray-600 text-sm">{a.serviceName}</td>
                      <td className="px-4 sm:px-6 py-3 sm:py-4 text-gray-600 text-sm">{a.slotDate}</td>
                      <td className="px-4 sm:px-6 py-3 sm:py-4 text-gray-600 text-sm">{formatTime(a.startTime)} – {formatTime(a.endTime)}</td>
                      <td className="px-4 sm:px-6 py-3 sm:py-4">
                        <StatusBadge status={a.status} />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
}
