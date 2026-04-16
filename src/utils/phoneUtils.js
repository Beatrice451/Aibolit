export const formatPhoneDisplay = (value) => {
  if (!value) return '';
  let digits = value.replace(/\D/g, '');
  
  if (digits.startsWith('8')) {
    digits = '7' + digits.substring(1);
  } else if (digits.length > 0 && !digits.startsWith('7')) {
    digits = '7' + digits;
  }
  
  if (digits.length === 0) return '';
  
  let result = '+7';
  if (digits.length > 1) result += ' (' + digits.substring(1, 4);
  if (digits.length > 4) result += ') ' + digits.substring(4, 7);
  if (digits.length > 7) result += '-' + digits.substring(7, 9);
  if (digits.length > 9) result += '-' + digits.substring(9, 11);
  
  return result;
};

export const formatPhoneForApi = (value) => {
  if (!value) return '';
  let digits = value.replace(/\D/g, '');
  
  if (digits.startsWith('8')) {
    return '+7' + digits.substring(1);
  } else if (digits.startsWith('7')) {
    return '+' + digits;
  }
  return '+7' + digits;
};