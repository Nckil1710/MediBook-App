import { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import DashboardHeader from '../components/DashboardHeader';
import { admin } from '../api/client';

function formatTime(t) {
  return t ? String(t).slice(0, 5) : '';
}

function StatusBadge({ status }) {
  const styles = {
    PENDING: 'bg-amber-100 text-amber-800',
    APPROVED: 'bg-green-100 text-green-800',
    REJECTED: 'bg-red-100 text-red-800',
    COMPLETED: 'bg-blue-100 text-blue-800',
  };
  return (
    <span className={`px-3 py-1 rounded-full text-sm font-medium ${styles[status] || 'bg-gray-100'}`}>
      {status}
    </span>
  );
}

export default function AdminAppointments() {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [updating, setUpdating] = useState(null);

  useEffect(() => {
    admin.getAppointments().then(({ data }) => setList(data)).catch(() => setError('Failed to load appointments')).finally(() => setLoading(false));
  }, []);

  const handleStatus = async (id, status) => {
    setUpdating(id);
    setError('');
    try {
      const { data } = await admin.updateAppointmentStatus(id, status);
      setList((prev) => prev.map((a) => (a.id === id ? data : a)));
    } catch (err) {
      setError(err.response?.data?.error || 'Update failed');
    } finally {
      setUpdating(null);
    }
  };

  return (
    <Layout>
      <DashboardHeader />
      <div className="p-4 sm:p-6 lg:p-8">
        <h1 className="text-xl sm:text-2xl font-bold text-gray-900 mb-4 sm:mb-6">All appointments</h1>
        {error && <div className="mb-4 p-4 bg-red-50 text-red-700 rounded-lg">{error}</div>}
        {loading ? (
          <p className="text-gray-500">Loading...</p>
        ) : list.length === 0 ? (
          <p className="text-gray-500">No appointments.</p>
        ) : (
          <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full min-w-[600px]">
                <thead>
                  <tr className="bg-gray-50 text-left text-xs sm:text-sm font-medium text-gray-500">
                    <th className="px-4 sm:px-6 py-3">Patient</th>
                    <th className="px-4 sm:px-6 py-3">Doctor / Service</th>
                    <th className="px-4 sm:px-6 py-3">Date & Time</th>
                    <th className="px-4 sm:px-6 py-3">Status</th>
                    <th className="px-4 sm:px-6 py-3">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {list.map((a) => (
                    <tr key={a.id} className="hover:bg-gray-50">
                      <td className="px-4 sm:px-6 py-3 sm:py-4">
                        <p className="font-medium text-gray-900 text-sm">{a.userName}</p>
                        <p className="text-xs sm:text-sm text-gray-500">{a.userEmail}</p>
                      </td>
                      <td className="px-4 sm:px-6 py-3 sm:py-4">
                        <p className="font-medium text-gray-900 text-sm">{a.doctorName || '—'}</p>
                        <p className="text-xs sm:text-sm text-gray-500">{a.serviceName}</p>
                      </td>
                      <td className="px-4 sm:px-6 py-3 sm:py-4 text-gray-600 text-sm">{a.slotDate} · {formatTime(a.startTime)} – {formatTime(a.endTime)}</td>
                      <td className="px-4 sm:px-6 py-3 sm:py-4">
                        <StatusBadge status={a.status} />
                      </td>
                      <td className="px-4 sm:px-6 py-3 sm:py-4">
                        {a.status === 'PENDING' && (
                          <div className="flex gap-2">
                            <button
                              type="button"
                              onClick={() => handleStatus(a.id, 'APPROVED')}
                              disabled={updating === a.id}
                              className="px-3 py-1.5 bg-green-100 text-green-800 text-sm font-medium rounded-lg hover:bg-green-200 disabled:opacity-50"
                            >
                              Accept
                            </button>
                            <button
                              type="button"
                              onClick={() => handleStatus(a.id, 'REJECTED')}
                              disabled={updating === a.id}
                              className="px-3 py-1.5 bg-red-100 text-red-800 text-sm font-medium rounded-lg hover:bg-red-200 disabled:opacity-50"
                            >
                              Reject
                            </button>
                          </div>
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
