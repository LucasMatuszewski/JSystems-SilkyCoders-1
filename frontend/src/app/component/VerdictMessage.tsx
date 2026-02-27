'use client';

const VERDICT_PATTERNS = [
  /^Zwrot możliwy\s*✓/i,
  /^Zwrot niemożliwy\s*✗/i,
  /^Reklamacja uzasadniona\s*✓/i,
  /^Reklamacja nieuzasadniona\s*✗/i,
  /^Return (approved|possible)\s*✓/i,
  /^Return (rejected|not possible)\s*✗/i,
  /^Complaint (accepted|valid)\s*✓/i,
  /^Complaint (rejected|invalid)\s*✗/i,
];

export function isVerdictMessage(text: string): boolean {
  if (!text) return false;
  const firstLine = text.split('\n')[0].trim();
  return VERDICT_PATTERNS.some(pattern => pattern.test(firstLine));
}

interface VerdictMessageProps {
  text: string;
}

export function VerdictMessage({ text }: VerdictMessageProps) {
  const lines = text.split('\n').filter(l => l.trim());
  const conclusionLine = lines[0] ?? '';
  const restLines = lines.slice(1);

  const isApproved = conclusionLine.includes('✓');
  const verdictType = isApproved ? 'approved' : 'rejected';

  return (
    <div
      data-testid={isApproved ? 'verdict-approved' : 'verdict-rejected'}
      className={[
        'rounded-2xl overflow-hidden w-full',
        'border',
        isApproved ? 'border-[#0DB209]/30' : 'border-[#FF0023]/30',
      ].join(' ')}
      style={{ boxShadow: '0px 2px 12px rgba(24,25,26,0.08), 0px 1px 2px rgba(26,13,0,0.06)' }}
    >
      {/* Verdict banner */}
      <div
        className={[
          'px-5 py-4 flex items-center gap-3',
          isApproved
            ? 'bg-[#0DB209]/10 border-b border-[#0DB209]/20'
            : 'bg-[#FF0023]/8 border-b border-[#FF0023]/20',
        ].join(' ')}
        style={isApproved ? { backgroundColor: 'rgba(13,178,9,0.08)' } : { backgroundColor: 'rgba(255,0,35,0.06)' }}
      >
        {/* Icon */}
        <div
          className={[
            'flex-shrink-0 flex items-center justify-center w-8 h-8 rounded-full',
            isApproved ? 'bg-[#0DB209]' : 'bg-[#FF0023]',
          ].join(' ')}
          aria-hidden="true"
        >
          {isApproved ? (
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#FFFFFF" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
          ) : (
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#FFFFFF" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          )}
        </div>

        {/* Conclusion */}
        <p
          data-verdict={verdictType}
          className={[
            'text-base font-semibold leading-tight',
            isApproved ? 'text-[#0A8C07]' : 'text-[#CC001C]',
          ].join(' ')}
        >
          {conclusionLine}
        </p>
      </div>

      {/* Justification + next steps */}
      {restLines.length > 0 && (
        <div className="bg-white px-5 py-4 space-y-2">
          {restLines.map((line, i) => (
            <p key={i} className="text-sm text-[#494A4D] leading-relaxed">
              {line}
            </p>
          ))}
        </div>
      )}
    </div>
  );
}
