import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { IntakeForm } from '../components/IntakeForm';

describe('IntakeForm', () => {
  it('renders the form with all required fields', () => {
    const onSubmit = vi.fn();
    render(<IntakeForm onSubmit={onSubmit} />);

    expect(screen.getByText('Numer zamówienia')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('np. PL-123456')).toBeInTheDocument();
    expect(screen.getByText('Zwrot standardowy')).toBeInTheDocument();
    expect(screen.getByText('Reklamacja')).toBeInTheDocument();
    expect(
      screen.getByPlaceholderText(
        'Opisz krótko przyczynę zwrotu lub wadę produktu...',
      ),
    ).toBeInTheDocument();
  });

  it('shows validation error for short order ID', async () => {
    const onSubmit = vi.fn();
    render(<IntakeForm onSubmit={onSubmit} />);

    const orderInput = screen.getByPlaceholderText('np. PL-123456');
    const descInput = screen.getByPlaceholderText(
      'Opisz krótko przyczynę zwrotu lub wadę produktu...',
    );
    const submitBtn = screen.getByText('Rozpocznij weryfikację');

    fireEvent.change(orderInput, { target: { value: 'ABC' } });
    fireEvent.change(descInput, {
      target: { value: 'This is a long description' },
    });
    fireEvent.click(submitBtn);

    await waitFor(() => {
      expect(
        screen.getByText('Numer zamówienia musi mieć co najmniej 5 znaków'),
      ).toBeInTheDocument();
    });
  });

  it('shows validation error for short description', async () => {
    const onSubmit = vi.fn();
    render(<IntakeForm onSubmit={onSubmit} />);

    const orderInput = screen.getByPlaceholderText('np. PL-123456');
    const descInput = screen.getByPlaceholderText(
      'Opisz krótko przyczynę zwrotu lub wadę produktu...',
    );
    const submitBtn = screen.getByText('Rozpocznij weryfikację');

    fireEvent.change(orderInput, { target: { value: 'PL-12345' } });
    fireEvent.change(descInput, { target: { value: 'Short' } });
    fireEvent.click(submitBtn);

    await waitFor(() => {
      expect(
        screen.getByText('Opis musi mieć co najmniej 10 znaków'),
      ).toBeInTheDocument();
    });
  });
});
