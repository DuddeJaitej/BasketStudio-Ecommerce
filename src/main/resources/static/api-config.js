/* Use the deployed API explicitly so auth pages work on Railway and Vercel. */
window.BASKET_API_URL = 'https://basketstudio-ecommerce-production.up.railway.app';
const basketOriginalFetch = window.fetch.bind(window);
window.fetch = (resource, options) => {
    if (typeof resource === 'string' && resource.startsWith('/api') && window.BASKET_API_URL) {
        return basketOriginalFetch(`${window.BASKET_API_URL}${resource}`, options);
    }
    return basketOriginalFetch(resource, options);
};
