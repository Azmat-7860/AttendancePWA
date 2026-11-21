import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import App from './App';
import './i18n/config';

describe('App', () => {
  it('renders login screen when unauthenticated', () => {
    render(<App />);
    expect(screen.getByText('Worker Attendance')).toBeInTheDocument();
    expect(screen.getByText('Sign in as Owner (Alice)')).toBeInTheDocument();
  });
});
