export function toIsoFromLocal(datetimeLocal) {
  if (!datetimeLocal) {
    return null;
  }
  return new Date(datetimeLocal).toISOString();
}

export function nowPlusDays(days) {
  const date = new Date();
  date.setDate(date.getDate() + days);
  return date;
}

export function toDatetimeLocal(date) {
  const copy = new Date(date);
  const tzOffset = copy.getTimezoneOffset() * 60000;
  return new Date(copy.getTime() - tzOffset).toISOString().slice(0, 16);
}

export function shortMoney(value) {
  if (value === null || value === undefined) {
    return "-";
  }
  return new Intl.NumberFormat("it-IT", {
    style: "currency",
    currency: "EUR",
  }).format(Number(value));
}
