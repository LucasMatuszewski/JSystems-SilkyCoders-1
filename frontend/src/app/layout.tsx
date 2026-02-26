import './globals.css';
import { ReactNode } from 'react';
import { CopilotKit } from '@copilotkit/react-core';
import '@copilotkit/react-ui/styles.css';

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="pl">
      <body className="h-screen overflow-hidden flex flex-col">
        <CopilotKit runtimeUrl="/api/langgraph4j" agent="default">
          {children}
        </CopilotKit>
      </body>
    </html>
  );
}
