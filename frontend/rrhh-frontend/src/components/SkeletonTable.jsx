export default function SkeletonTable({ cols = 4, rows = 5 }) {
  return (
    <table style={{ width: "100%", borderCollapse: "collapse" }}>
      <tbody>
        {Array.from({ length: rows }).map((_, r) => (
          <tr key={r} style={{ borderBottom: "1px solid var(--border, #e2e8f0)" }}>
            {Array.from({ length: cols }).map((_, c) => (
              <td key={c} style={{ padding: "12px 10px" }}>
                <div
                  className="skeleton-cell"
                  style={{ width: c === 0 ? "60%" : c === cols - 1 ? "40%" : "80%" }}
                />
              </td>
            ))}
          </tr>
        ))}
      </tbody>
    </table>
  );
}
