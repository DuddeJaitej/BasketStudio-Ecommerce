const form = document.querySelector('#loginForm');
form.addEventListener('submit', async event => {
	event.preventDefault();
	const button = form.querySelector('button');
	const error = document.querySelector('#loginError');
	button.disabled = true;
	error.textContent = '';
	try {
		const response = await fetch('/api/auth/login', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(Object.fromEntries(new FormData(form)))
		});
		if (!response.ok) {
			const problem = await response.json().catch(() => ({}));
			error.textContent = response.status === 401
				? 'Email or password is incorrect.'
				: problem.detail || problem.message || 'Login is temporarily unavailable.';
			return;
		}
		const user = await response.json();
		localStorage.setItem('basketToken', user.token);
		location.assign('/profile.html');
	} catch (requestError) {
		error.textContent = 'Could not connect to the account service. Please try again.';
	} finally {
		button.disabled = false;
	}
});
