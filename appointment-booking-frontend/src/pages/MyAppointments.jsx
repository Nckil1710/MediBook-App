import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import DashboardHeader from '../components/DashboardHeader';
import { Link } from 'react-router-dom';
import { appointments } from '../api/client';

function formatTime(t) {
  if (!t) return '';
  return String(t).slice(0, 5);
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

export default function MyAppointments() {
  const navigate = useNavigate();
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [cancelling, setCancelling] = useState(null);

  useEffect(() => {
    appointments.my().then(({ data }) => setList(data)).catch(() => setError('Failed to load appointments')).finally(() => setLoading(false));
  }, []);

  const handleCancel = async (id) => {
    setCancelling(id);
    setError('');
    try {
      await appointments.cancel(id);
      setList((prev) => prev.filter((a) => a.id !== id));
    } catch (err) {
      setError(err.response?.data?.error || 'Cancel failed');
    } finally {
      setCancelling(null);
    }
  };

  const canCancel = (a) => ['PENDING', 'APPROVED'].includes(a.status);
  const canReschedule = (a) => ['PENDING', 'APPROVED'].includes(a.status);

  return (
    <Layout>
      <DashboardHeader />
      <div className="p-4 sm:p-6 lg:p-8">
        <h1 className="text-xl sm:text-2xl font-bold text-gray-900 mb-4 sm:mb-6">My Appointments</h1>
        {error && <div className="mb-4 p-4 bg-red-50 text-red-700 rounded-lg">{error}</div>}
        <p className="mb-4 text-amber-700">
          To reschedule, click <strong>Reschedule</strong> to pick a new slot. You can also cancel and <Link to="/book" className="font-medium underline">book a new slot</Link>.
        </p>

        {loading ? (
          <p className="text-gray-500">Loading...</p>
        ) : list.length === 0 ? (
          <div className="bg-white rounded-xl border border-gray-200 p-8 text-center">
            <p className="text-gray-500 mb-4">No appointments yet.</p>
            <Link to="/book" className="inline-flex items-center gap-2 px-4 py-2 bg-teal-600 text-white rounded-lg font-medium hover:bg-teal-700">
              Book Appointment
            </Link>
          </div>
        ) : (
          <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full min-w-[600px]">
                <thead>
                  <tr className="bg-gray-50 text-left text-xs sm:text-sm font-medium text-gray-500">
                    <th className="px-4 sm:px-6 py-3">Doctor</th>
                    <th className="px-4 sm:px-6 py-3">Specialty</th>
                    <th className="px-4 sm:px-6 py-3">Date</th>
                    <th className="px-4 sm:px-6 py-3">Time</th>
                    <th className="px-4 sm:px-6 py-3">Status</th>
                    <th className="px-4 sm:px-6 py-3">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {list.map((a) => (
                    <tr key={a.id} className="hover:bg-gray-50">
                      <td className="px-4 sm:px-6 py-3 sm:py-4 font-medium text-gray-900 text-sm">{a.doctorName || '—'}</td>
                      <td className="px-4 sm:px-6 py-3 sm:py-4 text-gray-600 text-sm">{a.serviceName}</td>
                      <td className="px-4 sm:px-6 py-3 sm:py-4 text-gray-600 text-sm">{a.slotDate}</td>
                      <td className="px-4 sm:px-6 py-3 sm:py-4 text-gray-600 text-sm">{formatTime(a.startTime)} – {formatTime(a.endTime)}</td>
                      <td className="px-4 sm:px-6 py-3 sm:py-4">
                        <StatusBadge status={a.status} />
                      </td>
                      <td className="px-4 sm:px-6 py-3 sm:py-4 flex gap-2 flex-wrap">
                        {canReschedule(a) && (
                          <button
                            type="button"
                            onClick={() => navigate('/book', { state: { rescheduleAppointment: a } })}
                            className="text-sm text-teal-600 hover:underline"
                          >
                            Reschedule
                          </button>
                        )}
                        {canCancel(a) && (
                          <button
                            type="button"
                            onClick={() => handleCancel(a.id)}
                            disabled={cancelling === a.id}
                            className="text-sm text-red-600 hover:underline disabled:opacity-50"
                          >
                            {cancelling === a.id ? 'Cancelling...' : 'Cancel'}
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </Layout>
  );
}
