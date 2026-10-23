import { useEffect, useRef } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

const dot = (color) => ({ radius: 9, color: '#fff', weight: 2, fillColor: color, fillOpacity: 1 });

export default function MapView({ pickup, drop, car }) {
  const el = useRef(null), map = useRef(null), layer = useRef(null);

  useEffect(() => {
    map.current = L.map(el.current).setView([26.9124, 75.7873], 12);
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19, attribution: '© OpenStreetMap contributors' }).addTo(map.current);
    layer.current = L.layerGroup().addTo(map.current);
    return () => map.current.remove();
  }, []);

  useEffect(() => {
    const g = layer.current; g.clearLayers();
    const pts = [];
    if (pickup) { L.circleMarker(pickup, dot('#1f9d6b')).addTo(g); pts.push(pickup); }
    if (drop) { L.circleMarker(drop, dot('#d6455d')).addTo(g); pts.push(drop); }
    if (pickup && drop) L.polyline([pickup, drop], { color: '#f2a900', weight: 4, dashArray: '6 8' }).addTo(g);
    if (car) L.marker(car, { icon: L.divIcon({ html: '🚖', className: 'car', iconSize: [28, 28] }) }).addTo(g);
    if (pts.length > 1 && !car) map.current.fitBounds(pts, { padding: [50, 50] });
  }, [pickup, drop, car]);

  return <div ref={el} className="h-[380px] overflow-hidden rounded-2xl border border-line lg:h-[580px]" />;
}
