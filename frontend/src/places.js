// Demo locations in Jaipur: [lat, lng]
export const PLACES = {
  'Jaipur Junction': [26.9196, 75.7878], 'Hawa Mahal': [26.9239, 75.8267], 'Albert Hall': [26.9115, 75.8195],
  'Birla Mandir': [26.8925, 75.8156], 'World Trade Park': [26.8545, 75.7873], 'Airport (JAI)': [26.8242, 75.8122],
  'Jal Mahal': [26.9535, 75.846], 'Amer Fort': [26.9855, 75.8513],
};
export const TYPES = { MINI: 'Mini', SEDAN: 'Sedan', PRIME: 'Prime' };
export const inr = (n) => '₹' + Number(n).toLocaleString('en-IN');
export const STATUS = { REQUESTED: 'Finding a driver', ACCEPTED: 'Driver is on the way', ARRIVED: 'Driver has arrived', IN_TRIP: 'Trip in progress', COMPLETED: 'Trip completed', PAID: 'Paid', CANCELLED: 'Cancelled' };
export const ORDER = ['REQUESTED', 'ACCEPTED', 'ARRIVED', 'IN_TRIP', 'COMPLETED', 'PAID'];
