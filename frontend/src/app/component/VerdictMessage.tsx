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
      className="rounded-lg border border-[#E3E4E5] p-4 my-2 bg-white shadow-sm max-w-sm"
    >
      {/* Conclusion line */}
      <p
        data-verdict={verdictType}
        className={`text-base font-semibold mb-2 ${
          isApproved ? 'text-[#0A8C07]' : 'text-[#CC001C]'
        }`}
      >
        {conclusionLine}
      </p>

      {/* Divider */}
      <hr className={`mb-2 ${isApproved ? 'border-[#0DB209]/30' : 'border-[#FF0023]/30'}`} />

      {/* Justification + next steps */}
      {restLines.map((line, i) => (
        <p key={i} className="text-sm text-[#494A4D] mb-1 last:mb-0">
          {line}
        </p>
      ))}
    </div>
  );
}
