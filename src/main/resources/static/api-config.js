/* Use the local Spring Boot API during development. */
window.BASKET_API_URL = 'http://localhost:8086';
const basketOriginalFetch = window.fetch.bind(window);
window.fetch = (resource, options) => {
    if (typeof resource === 'string' && resource.startsWith('/api') && window.BASKET_API_URL) {
        return basketOriginalFetch(`${window.BASKET_API_URL}${resource}`, options);
    }
    return basketOriginalFetch(resource, options);
};
