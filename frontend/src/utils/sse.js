export const subscribeToHoaDon = (idHoaDon, onUpdate) => {
  const eventSource = new EventSource(
    `http://localhost:8080/api/admin/hoa-don/subscribe/${idHoaDon}`
  );

  eventSource.addEventListener('update', (event) => {
    try {
      const data = JSON.parse(event.data);
      onUpdate(data);
    } catch (e) {
      console.error('Error parsing SSE data:', e);
    }
  });

  eventSource.onerror = () => {
    eventSource.close();
  };

  return eventSource;
};
