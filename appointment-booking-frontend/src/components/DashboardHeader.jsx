import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function DashboardHeader({ title, showBookButton = false }) {
  const { user } = useAuth();

  return (
    <header className="bg-white border-b border-gray-200 px-4 sm:px-6 lg:px-8 py-3 sm:py-4 flex items-center justify-between gap-3 min-w-0">
      <div className="flex-1" />
      <div className="flex items-center gap-2 sm:gap-4 shrink-0">
        {showBookButton && (
          <Link
            to="/book"
            className="inline-flex items-center gap-2 px-3 py-2 sm:px-4 bg-green-500 text-white rounded-lg font-medium hover:bg-green-600 transition-colors text-sm sm:text-base"
          >
            <span>📅</span>
            <span className="hidden sm:inline">Book Appointment</span>
          </Link>
        )}
        <Link
          to="/profile"
          className="w-9 h-9 sm:w-10 sm:h-10 rounded-full bg-teal-500 flex items-center justify-center font-semibold text-white hover:ring-2 hover:ring-teal-300 transition-shadow text-sm sm:text-base shrink-0"
          aria-label="Profile"
        >
          {user?.name?.charAt(0)?.toUpperCase() || 'U'}
        </Link>
      </div>
    </header>
  );
}
