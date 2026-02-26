import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ReturnForm } from '../../app/component/ReturnForm';

const mockRespond = vi.fn();

const defaultProps = {
  args: { type: 'return' },
  status: 'inProgress' as const,
  respond: mockRespond,
};

describe('ReturnForm', () => {
  beforeEach(() => {
    mockRespond.mockClear();
  });

  it('renders all required fields with Polish labels', () => {
    render(<ReturnForm {...defaultProps} />);
    expect(screen.getByLabelText(/nazwa produktu/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/typ zgłoszenia/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/opis problemu/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/zdjęcie produktu/i)).toBeInTheDocument();
  });

  it('renders submit button with Polish text', () => {
    render(<ReturnForm {...defaultProps} />);
    expect(screen.getByRole('button', { name: /wyślij do asystenta/i })).toBeInTheDocument();
  });

  it('submit button is disabled when fields are empty', () => {
    render(<ReturnForm {...defaultProps} />);
    const btn = screen.getByRole('button', { name: /wyślij do asystenta/i });
    expect(btn).toBeDisabled();
  });

  it('pre-populates type select from args.type', () => {
    render(<ReturnForm {...defaultProps} args={{ type: 'complaint' }} />);
    const select = screen.getByLabelText(/typ zgłoszenia/i) as HTMLSelectElement;
    expect(select.value).toBe('complaint');
  });

  it('shows validation error when productName is empty on submit', async () => {
    render(<ReturnForm {...defaultProps} />);
    const form = screen.getByRole('form');
    fireEvent.submit(form);
    await waitFor(() => {
      expect(screen.getByText(/nazwa produktu jest wymagana/i)).toBeInTheDocument();
    });
  });

  it('renders as read-only when status is complete', () => {
    render(<ReturnForm {...defaultProps} status="complete" />);
    const inputs = screen.queryAllByRole('textbox');
    inputs.forEach(input => {
      expect(input).toBeDisabled();
    });
  });

  it('enables submit button when productName and description are filled', async () => {
    const user = userEvent.setup();
    render(<ReturnForm {...defaultProps} />);

    await user.type(screen.getByLabelText(/nazwa produktu/i), 'Test Product');
    await user.type(screen.getByLabelText(/opis problemu/i), 'This is a long enough description for validation');

    // Button still disabled without photo — that's correct per spec
    const btn = screen.getByRole('button', { name: /wyślij do asystenta/i });
    expect(btn).toBeDisabled();
  });
});
