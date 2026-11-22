const DEFAULT_IMAGE = 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?auto=format&fit=crop&w=900&q=80';

// RoomCategory is the current backend shape. Keep the older display names
// accepted while pages migrate away from the previous room type API.
export function normalizeRoomCategory(category) {
  const name = category.categoryName || category.name || '';
  const normalizedName = name.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
  const roomType = category.roomType || category.room_type || (
    normalizedName.includes('view bien') || normalizedName.includes('sea view') ? 'DOUBLE_SEA_VIEW' :
    normalizedName.includes('view pho') || normalizedName.includes('city view') || normalizedName.includes('phong doi thuong') ? 'DOUBLE_CITY_VIEW' :
    normalizedName.includes('phong ba') || normalizedName.includes('triple') ? 'TRIPLE' :
    normalizedName.includes('phong don') || normalizedName.includes('single') ? 'SINGLE' : ''
  );
  const amenities = category.amenities;

  return {
    ...category,
    id: String(category.id),
    name,
    roomType,
    basePrice: Number(category.price ?? category.basePrice ?? 0),
    maxAdults: Number(category.maxGuests ?? category.maxAdults ?? 0),
    maxChildren: category.maxChildren,
    amenities: Array.isArray(amenities)
      ? amenities
      : String(amenities || '').split(',').map(item => item.trim()).filter(Boolean),
    viewType: roomType === 'DOUBLE_SEA_VIEW' ? 'sea_view' : category.viewType,
    imageUrl: category.imageUrl || DEFAULT_IMAGE,
  };
}
