export function formatCurrencyARS(amount: number | null | undefined): string {
  const value = typeof amount === 'number' ? amount : 0

  return new Intl.NumberFormat('es-AR', {
    style: 'currency',
    currency: 'ARS',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(value)
}
