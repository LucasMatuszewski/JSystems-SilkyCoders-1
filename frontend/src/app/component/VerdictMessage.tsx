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
    <div className="rounded-lg border p-4 my-2 bg-white shadow-sm max-w-sm">
      {/* Conclusion line */}
      <p
        data-verdict={verdictType}
        className={`text-base font-semibold mb-2 ${
          isApproved ? 'text-green-700' : 'text-red-700'
        }`}
      >
        {conclusionLine}
      </p>

      {/* Divider */}
      <hr className={`mb-2 ${isApproved ? 'border-green-200' : 'border-red-200'}`} />

      {/* Justification + next steps */}
      {restLines.map((line, i) => (
        <p key={i} className="text-sm text-gray-700 mb-1 last:mb-0">
          {line}
        </p>
      ))}
    </div>
  );
}
