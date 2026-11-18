import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import App from './App';

describe('App', () => {
  it('renders the application title and ready status', () => {
    render(<App />);
    expect(screen.getByText('Worker Attendance PWA')).toBeInTheDocument();
    expect(screen.getByText('System Shell Ready')).toBeInTheDocument();
  });
});

