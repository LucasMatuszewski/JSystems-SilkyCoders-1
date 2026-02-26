import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';

// Mock CopilotKit modules
vi.mock('@copilotkit/react-core', () => ({
  useCopilotAction: vi.fn(),
}));
vi.mock('@copilotkit/react-ui', () => ({
  CopilotChat: ({ labels }: { labels: { title?: string; initial?: string } }) => (
    <div>
      <span>{labels?.title}</span>
      <span>{labels?.initial}</span>
    </div>
  ),
}));
vi.mock('../../app/component/ReturnForm', () => ({
  ReturnForm: () => <div>ReturnForm</div>,
}));

import { SinsayChat } from '../../app/component/SinsayChat';

describe('SinsayChat', () => {
  it('renders CopilotChat with Sinsay title', () => {
    render(<SinsayChat />);
    expect(screen.getByText('Sinsay AI Assistant')).toBeInTheDocument();
  });

  it('renders initial message in Polish', () => {
    render(<SinsayChat />);
    expect(screen.getByText(/Cześć/)).toBeInTheDocument();
  });

  it('renders branded header with Sinsay logo and AI Assistant label', () => {
    render(<SinsayChat />);
    const header = screen.getByRole('banner', { name: 'Sinsay AI Assistant' });
    expect(header).toBeInTheDocument();
    // Logo SVG should be present with accessible name
    const logo = screen.getByRole('img', { name: 'Sinsay' });
    expect(logo).toBeInTheDocument();
    // "AI Assistant" text label should be present
    expect(screen.getByText('AI Assistant')).toBeInTheDocument();
  });
});
