import { Route, Routes } from 'react-router';
import { useAuth } from './context/AuthContext';
import { LoginPage } from './components/LoginPage';
import { AppLayout } from './components/AppLayout';
import { Dashboard } from './components/Dashboard';
import { SparePartsPage } from './components/SparePartsPage';
import { MachineDetailPage } from './components/MachineDetailPage';

function App() {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <LoginPage />;
  }

  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route path="/" element={<Dashboard />} />
        <Route path="/spare-parts" element={<SparePartsPage />} />
        <Route path="/machines/:id" element={<MachineDetailPage />} />
      </Route>
    </Routes>
  );
}

export default App;
