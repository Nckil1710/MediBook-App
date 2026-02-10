import Layout from '../components/Layout';
import DashboardHeader from '../components/DashboardHeader';
import { useAuth } from '../context/AuthContext';

export default function Profile() {
  const { user } = useAuth();

  return (
    <Layout>
      <DashboardHeader />
      <div className="p-4 sm:p-6 lg:p-8">
        <div className="max-w-md mx-auto">
          <h1 className="text-2xl font-bold text-gray-900 mb-6">Profile</h1>
          <div className="bg-white rounded-xl border border-gray-200 p-6">
          <div className="flex items-center gap-4 mb-6">
            <div className="w-16 h-16 rounded-full bg-teal-500 flex items-center justify-center font-bold text-white text-2xl">
              {user?.name?.charAt(0)?.toUpperCase() || 'U'}
            </div>
            <div>
              <p className="text-lg font-semibold text-gray-900">{user?.name}</p>
              <p className="text-sm text-gray-500">{user?.email}</p>
              <p className="text-xs text-gray-400 mt-1">{user?.role === 'ADMIN' ? 'Admin' : 'Patient'}</p>
            </div>
          </div>
          <dl className="space-y-3 text-sm">
            <div>
              <dt className="text-gray-500">Name</dt>
              <dd className="font-medium text-gray-900">{user?.name}</dd>
            </div>
            <div>
              <dt className="text-gray-500">Email</dt>
              <dd className="font-medium text-gray-900">{user?.email}</dd>
            </div>
            <div>
              <dt className="text-gray-500">Role</dt>
              <dd className="font-medium text-gray-900">{user?.role === 'ADMIN' ? 'Admin' : 'Patient'}</dd>
            </div>
          </dl>
          </div>
        </div>
      </div>
    </Layout>
  );
}
