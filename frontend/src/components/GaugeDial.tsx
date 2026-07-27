interface GaugeDialProps {
  value: number;
  min: number;
  max: number;
  warning: number;
  critical: number;
  unit: string;
  label: string;
}

const SWEEP_DEG = 260;
const START_DEG = -130;

function zoneColor(value: number, warning: number, critical: number) {
  if (value >= critical) return 'var(--color-signal-red)';
  if (value >= warning) return 'var(--color-signal-amber)';
  return 'var(--color-signal-green)';
}

export function GaugeDial({ value, min, max, warning, critical, unit, label }: GaugeDialProps) {
  const clamped = Math.min(Math.max(value, min), max);
  const fraction = (clamped - min) / (max - min);
  const angle = START_DEG + fraction * SWEEP_DEG;
  const color = zoneColor(value, warning, critical);

  const radius = 40;
  const cx = 50;
  const cy = 54;

  const toXY = (deg: number) => {
    const rad = (deg * Math.PI) / 180;
    return [cx + radius * Math.cos(rad), cy + radius * Math.sin(rad)];
  };

  const arcPath = (fromDeg: number, toDeg: number) => {
    const [x1, y1] = toXY(fromDeg);
    const [x2, y2] = toXY(toDeg);
    const large = toDeg - fromDeg > 180 ? 1 : 0;
    return `M ${x1} ${y1} A ${radius} ${radius} 0 ${large} 1 ${x2} ${y2}`;
  };

  const [needleX, needleY] = toXY(angle);

  return (
    <div className="flex flex-col items-center">
      <svg viewBox="0 0 100 78" className="w-28 h-auto">
        {/* track */}
        <path
          d={arcPath(START_DEG, START_DEG + SWEEP_DEG)}
          fill="none"
          stroke="var(--color-steel)"
          strokeWidth="6"
          strokeLinecap="round"
        />
        {/* filled progress */}
        <path
          d={arcPath(START_DEG, angle)}
          fill="none"
          stroke={color}
          strokeWidth="6"
          strokeLinecap="round"
          style={{ transition: 'all 600ms ease' }}
        />
        {/* needle */}
        <line
          x1={cx}
          y1={cy}
          x2={needleX}
          y2={needleY}
          stroke="var(--color-ink-100)"
          strokeWidth="1.5"
          style={{ transition: 'all 600ms ease' }}
        />
        <circle cx={cx} cy={cy} r="2.5" fill="var(--color-ink-100)" />
      </svg>
      <div className="-mt-2 text-center">
        <div className="font-mono text-lg font-medium" style={{ color }}>
          {value.toFixed(1)}
          <span className="text-xs text-ink-300 ml-0.5">{unit}</span>
        </div>
        <div className="text-[10px] uppercase tracking-wider text-ink-500">{label}</div>
      </div>
    </div>
  );
}
