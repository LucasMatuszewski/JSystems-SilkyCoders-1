import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { VerdictMessage, isVerdictMessage } from '../../app/component/VerdictMessage';

describe('isVerdictMessage', () => {
  it('detects approved return verdict', () => {
    expect(isVerdictMessage('Zwrot możliwy ✓\nBo produkt jest nieużywany.')).toBe(true);
  });

  it('detects rejected return verdict', () => {
    expect(isVerdictMessage('Zwrot niemożliwy ✗\nProdukt posiada ślady użytkowania.')).toBe(true);
  });

  it('detects approved complaint verdict', () => {
    expect(isVerdictMessage('Reklamacja uzasadniona ✓\nWada fabryczna.')).toBe(true);
  });

  it('detects rejected complaint verdict', () => {
    expect(isVerdictMessage('Reklamacja nieuzasadniona ✗\nUszkodzenie mechaniczne.')).toBe(true);
  });

  it('returns false for regular chat messages', () => {
    expect(isVerdictMessage('Cześć! Jak mogę Ci pomóc?')).toBe(false);
  });

  it('returns false for empty string', () => {
    expect(isVerdictMessage('')).toBe(false);
  });
});

describe('VerdictMessage', () => {
  it('renders approved verdict with conclusion', () => {
    render(<VerdictMessage text="Zwrot możliwy ✓\nProdukt jest nieużywany z metkami.\nOddaj w sklepie lub przez kuriera." />);
    expect(screen.getByText(/Zwrot możliwy/)).toBeInTheDocument();
    expect(screen.getByText(/✓/)).toBeInTheDocument();
  });

  it('renders rejected verdict', () => {
    render(<VerdictMessage text="Zwrot niemożliwy ✗\nProdukt posiada ślady użytkowania.\nSkontaktuj się z BOK." />);
    expect(screen.getByText(/Zwrot niemożliwy/)).toBeInTheDocument();
    expect(screen.getByText(/✗/)).toBeInTheDocument();
  });

  it('applies green styling for approved verdict (✓)', () => {
    const { container } = render(<VerdictMessage text="Reklamacja uzasadniona ✓\nWada fabryczna." />);
    const conclusionEl = container.querySelector('[data-verdict="approved"]');
    expect(conclusionEl).toBeInTheDocument();
  });

  it('applies red styling for rejected verdict (✗)', () => {
    const { container } = render(<VerdictMessage text="Reklamacja nieuzasadniona ✗\nUszkodzenie użytkownika." />);
    const conclusionEl = container.querySelector('[data-verdict="rejected"]');
    expect(conclusionEl).toBeInTheDocument();
  });

  it('renders justification text', () => {
    render(<VerdictMessage text="Zwrot możliwy ✓\nProdukt jest nieużywany.\nOddaj w sklepie." />);
    expect(screen.getByText(/Produkt jest nieużywany/)).toBeInTheDocument();
  });
});
