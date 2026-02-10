import Layout from '../components/Layout';
import DashboardHeader from '../components/DashboardHeader';

export default function AboutUs() {
  return (
    <Layout>
      <DashboardHeader title="About Us" />
      <div className="p-4 sm:p-6 lg:p-8">
        <div className="max-w-3xl">
          <div className="bg-white border border-gray-200 rounded-xl p-6 sm:p-8">
            <h1 className="text-2xl font-bold text-gray-900">MediBook Appointment</h1>
            <p className="mt-2 text-gray-600">
              MediBook is a simple appointment booking platform that helps patients find doctors by service,
              view available slots, and book appointments securely.
            </p>

            <div className="mt-6 grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="rounded-lg border border-gray-200 bg-gray-50 p-4">
                <h2 className="text-sm font-semibold text-gray-900">Our Mission</h2>
                <p className="mt-1 text-sm text-gray-600">
                  Make healthcare scheduling fast, transparent, and accessible.
                </p>
              </div>
              <div className="rounded-lg border border-gray-200 bg-gray-50 p-4">
                <h2 className="text-sm font-semibold text-gray-900">What You Can Do</h2>
                <p className="mt-1 text-sm text-gray-600">
                  Browse services, choose a doctor, book a slot, and manage your appointments.
                </p>
              </div>
            </div>

            <div className="mt-6">
              <h2 className="text-sm font-semibold text-gray-900">Why MediBook</h2>
              <ul className="mt-2 space-y-2 text-sm text-gray-600">
                <li className="flex gap-2">
                  <span className="text-teal-600">•</span>
                  <span>Quick booking with real-time slot availability</span>
                </li>
                <li className="flex gap-2">
                  <span className="text-teal-600">•</span>
                  <span>Simple appointment tracking and status updates</span>
                </li>
                <li className="flex gap-2">
                  <span className="text-teal-600">•</span>
                  <span>Clean UI built for desktop and mobile</span>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
}
