/* Set BASKET_API_URL to the public Spring Boot URL after deploying the backend. */
window.BASKET_API_URL = 'https://basketstudio-ecommerce-production.up.railway.app';
const basketOriginalFetch = window.fetch.bind(window);
window.fetch = (resource, options) => {
    if (typeof resource === 'string' && resource.startsWith('/api') && window.BASKET_API_URL) {
        return basketOriginalFetch(`${window.BASKET_API_URL}${resource}`, options);
    }
    return basketOriginalFetch(resource, options);
};
