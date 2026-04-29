import axiosInstance from './axiosInstance';

/**
 * Get all pharmacies
 * @returns {Promise} List of pharmacies
 */
export const getPharmacies = async () => {
  const response = await axiosInstance.get('/api/pharmacies');
  return response.data;
};

/**
 * Get a specific pharmacy by ID
 * @param {number} pharmacyId - Pharmacy ID
 * @returns {Promise} Pharmacy details
 */
export const getPharmacyById = async (pharmacyId) => {
  const response = await axiosInstance.get(`/api/pharmacies/${pharmacyId}`);
  return response.data;
};

export default {
  getPharmacies,
  getPharmacyById,
};
