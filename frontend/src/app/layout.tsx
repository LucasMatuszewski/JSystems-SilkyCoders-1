/* CopilotKit styles load FIRST so that globals.css overrides (same !important weight,
   later cascade position) can win over CopilotKit defaults. */
import '@copilotkit/react-ui/styles.css';
import './globals.css';
import { ReactNode } from 'react';
import { CopilotKit } from '@copilotkit/react-core';

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
