'use client';

import { useCopilotAction } from '@copilotkit/react-core';
import { CopilotChat } from '@copilotkit/react-ui';
import { ReturnForm } from './ReturnForm';

export function SinsayChat() {
  useCopilotAction({
    name: 'showReturnForm',
    description: 'Display return/complaint form in chat',
    parameters: [
      { name: 'type', type: 'string', description: 'return or complaint' },
    ],
    renderAndWaitForResponse(props) {
      const status = props.status as 'inProgress' | 'complete' | 'executing';
      const args = props.args as { type?: string };
      const respond = (props.respond ?? (() => undefined)) as (response: string) => void;
      return (
        <ReturnForm args={args} status={status} respond={respond} />
      );
    },
  });

  return (
    <CopilotChat
      instructions="You are a helpful Sinsay customer service assistant. Respond in the user's language. On first interaction say: Cześć! Jestem Twoim wirtualnym asystentem Sinsay. W czym mogę Ci dziś pomóc?"
      labels={{
        title: 'Sinsay AI Assistant',
        initial: 'Cześć! Jestem Twoim wirtualnym asystentem Sinsay. W czym mogę Ci dziś pomóc?',
        placeholder: 'Napisz wiadomość...',
      }}
      className="h-full"
    />
  );
}
