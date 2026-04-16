// utils/flattenCategories.js
export const flattenCategories = (categories, parentId = null) => {
    let flat = [];
    categories.forEach(cat => {
      flat.push({ id: cat.id, name: cat.name, parentId });
      if (cat.children && cat.children.length > 0) {
        flat = flat.concat(flattenCategories(cat.children, cat.id));
      }
    });
    return flat;
  };