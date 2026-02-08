import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { auth } from '../api/client';
import { useAuth } from '../context/AuthContext';

export default function Register() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [phone, setPhone] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (password !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    if (password.length < 6) {
      setError('Password must be at least 6 characters');
      return;
    }
    setLoading(true);
    try {
      const { data } = await auth.register({ name, email, password, phone: phone || undefined });
      login(data);
      navigate('/dashboard');
    } catch (err) {
      const msg = err.response?.data?.errors
        ? Object.values(err.response.data.errors).flat().join(' ')
        : err.response?.data?.error || 'Registration failed';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 px-4 py-8">
      <div className="w-full max-w-md bg-white rounded-2xl shadow-lg border border-gray-200 p-8">
        <div className="text-center mb-8">
          <span className="text-4xl">❤️</span>
          <h1 className="text-2xl font-bold text-gray-900 mt-2">MediBook Smart Appointments</h1>
          <p className="text-gray-500 mt-1">Create your account</p>
        </div>
        <form onSubmit={handleSubmit} className="space-y-4">
          {error && <div className="p-3 bg-red-50 text-red-700 rounded-lg text-sm">{error}</div>}
          <label className="block">
            <span className="text-sm font-medium text-gray-700">Name</span>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              className="mt-1 block w-full rounded-lg border border-gray-300 px-4 py-2 focus:ring-teal-500 focus:border-teal-500"
              placeholder="John Doe"
            />
          </label>
          <label className="block">
            <span className="text-sm font-medium text-gray-700">Email</span>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              className="mt-1 block w-full rounded-lg border border-gray-300 px-4 py-2 focus:ring-teal-500 focus:border-teal-500"
              placeholder="you@example.com"
            />
          </label>
          <label className="block">
            <span className="text-sm font-medium text-gray-700">Password</span>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={6}
              className="mt-1 block w-full rounded-lg border border-gray-300 px-4 py-2 focus:ring-teal-500 focus:border-teal-500"
              placeholder="At least 6 characters"
            />
          </label>
          <label className="block">
            <span className="text-sm font-medium text-gray-700">Confirm Password</span>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
              minLength={6}
              className="mt-1 block w-full rounded-lg border border-gray-300 px-4 py-2 focus:ring-teal-500 focus:border-teal-500"
              placeholder="Repeat password"
            />
          </label>
          <label className="block">
            <span className="text-sm font-medium text-gray-700">Phone (optional)</span>
            <input
              type="tel"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              className="mt-1 block w-full rounded-lg border border-gray-300 px-4 py-2 focus:ring-teal-500 focus:border-teal-500"
              placeholder="+1 234 567 8900"
            />
          </label>
          <button
            type="submit"
            disabled={loading}
            className="w-full py-2.5 px-4 bg-teal-600 text-white font-medium rounded-lg hover:bg-teal-700 disabled:opacity-50 transition-colors"
          >
            {loading ? 'Creating account...' : 'Register'}
          </button>
        </form>
        <p className="mt-6 text-center text-sm text-gray-500">
          Already have an account?{' '}
          <Link to="/login" className="font-medium text-teal-600 hover:underline">Sign in</Link>
        </p>
      </div>
    </div>
  );
}
