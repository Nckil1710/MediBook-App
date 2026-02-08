import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ProtectedRoute({ children, adminOnly }) {
  const { user, loading, isAdmin } = useAuth();
  const location = useLocation();

  if (loading) return <div style={{ padding: '2rem', textAlign: 'center', color: '#94a3b8' }}>Loading...</div>;
  if (!user) return <Navigate to="/login" state={{ from: location }} replace />;
  if (adminOnly && !isAdmin) return <Navigate to="/dashboard" replace />;
  return children;
}
