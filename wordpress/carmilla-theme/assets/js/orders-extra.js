/* My-account extras: returns (← ReturnRequestScreen), recurring orders
   (← RecurringOrdersScreen), settings theme toggle (← SettingsScreen). */
( function () {
	'use strict';
	var cfg = window.CarmillaData || {};

	function esc( s ) { var d = document.createElement( 'div' ); d.textContent = s == null ? '' : s; return d.innerHTML; }
	function api( path, opts ) {
		opts = opts || {};
		opts.credentials = 'same-origin';
		opts.headers = Object.assign( { 'X-WP-Nonce': cfg.nonce }, opts.headers || {} );
		return fetch( cfg.restUrl + path, opts ).then( function ( r ) { return r.json(); } );
	}
	function post( path, body ) {
		return api( path, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify( body || {} ) } );
	}

	/* ---- Returns ---- */
	var returnsRoot = document.getElementById( 'cb-returns' );
	if ( returnsRoot ) {
		var typeState = 'RETURN';
		returnsRoot.innerHTML =
			'<div class="card card--pad cb-ret-form">' +
			'<div class="cb-ret-chips">' +
			'<button type="button" class="cb-chip is-on" data-type="RETURN">مرجوعی</button>' +
			'<button type="button" class="cb-chip" data-type="EXCHANGE">تعویض</button>' +
			'</div>' +
			'<input type="text" class="cb-ret-title" placeholder="عنوان کالا">' +
			'<input type="number" class="cb-ret-order" placeholder="شماره سفارش (اختیاری)">' +
			'<textarea class="cb-ret-reason" rows="2" placeholder="دلیل درخواست"></textarea>' +
			'<button type="button" class="btn btn--primary cb-ret-submit">ثبت درخواست</button>' +
			'</div>' +
			'<h3 class="t-title-sm" style="margin-block-start:var(--sp-lg)">درخواست‌های من</h3>' +
			'<div class="cb-ret-list"><p class="t-body-sm t-muted">در حال بارگذاری…</p></div>';

		var listEl = returnsRoot.querySelector( '.cb-ret-list' );
		Array.prototype.forEach.call( returnsRoot.querySelectorAll( '.cb-chip' ), function ( c ) {
			c.addEventListener( 'click', function () {
				returnsRoot.querySelectorAll( '.cb-chip' ).forEach( function ( x ) { x.classList.remove( 'is-on' ); } );
				c.classList.add( 'is-on' );
				typeState = c.getAttribute( 'data-type' );
			} );
		} );

		function paintReturns( items ) {
			if ( ! items || ! items.length ) { listEl.innerHTML = '<p class="t-body-sm t-muted">هنوز درخواستی ثبت نشده.</p>'; return; }
			listEl.innerHTML = items.map( function ( r ) {
				var note = r.adminNote ? '<p class="cb-ret-note">پاسخ ادمین: ' + esc( r.adminNote ) + '</p>' : '';
				return '<div class="card card--pad cb-ret-item">' +
					'<div class="cb-ret-item__head"><strong>' + esc( r.itemTitle ) + '</strong>' +
					'<span class="badge">' + esc( ( r.type === 'EXCHANGE' ? 'تعویض' : 'مرجوعی' ) + ' · ' + r.statusLabel ) + '</span></div>' +
					'<p class="t-body-sm t-muted">' + esc( r.reason ) + '</p>' + note + '</div>';
			} ).join( '' );
		}
		api( 'returns' ).then( paintReturns );

		returnsRoot.querySelector( '.cb-ret-submit' ).addEventListener( 'click', function () {
			var reason = returnsRoot.querySelector( '.cb-ret-reason' ).value.trim();
			if ( ! reason ) { return; }
			post( 'returns', {
				type: typeState,
				itemTitle: returnsRoot.querySelector( '.cb-ret-title' ).value.trim(),
				orderId: parseInt( returnsRoot.querySelector( '.cb-ret-order' ).value, 10 ) || 0,
				reason: reason
			} ).then( function () {
				returnsRoot.querySelector( '.cb-ret-reason' ).value = '';
				returnsRoot.querySelector( '.cb-ret-title' ).value = '';
				api( 'returns' ).then( paintReturns );
			} );
		} );
	}

	/* ---- Recurring orders ---- */
	var recRoot = document.getElementById( 'cb-recurring' );
	if ( recRoot ) {
		recRoot.innerHTML = '<div class="cb-rec-list"><p class="t-body-sm t-muted">در حال بارگذاری…</p></div>';
		var recList = recRoot.querySelector( '.cb-rec-list' );
		function paintRec( items ) {
			if ( ! items || ! items.length ) { recList.innerHTML = '<p class="t-body-sm t-muted">هنوز خرید تکراری‌ای ثبت نکرده‌اید.</p>'; return; }
			recList.innerHTML = items.map( function ( r ) {
				return '<div class="card card--pad cb-rec-item" data-id="' + r.id + '">' +
					'<div><strong>' + esc( r.productName ) + '</strong>' +
					'<p class="t-body-sm t-muted">هر ' + esc( r.intervalDays ) + ' روز · تعداد ' + esc( r.qty ) + ' · بعدی: ' + esc( r.nextRunAt ) + '</p></div>' +
					'<button type="button" class="btn btn--ghost cb-rec-cancel">لغو</button></div>';
			} ).join( '' );
			Array.prototype.forEach.call( recList.querySelectorAll( '.cb-rec-item' ), function ( el ) {
				el.querySelector( '.cb-rec-cancel' ).addEventListener( 'click', function () {
					if ( ! confirm( 'این خرید تکراری لغو شود؟' ) ) { return; }
					post( 'recurring/' + el.getAttribute( 'data-id' ) + '/cancel', {} ).then( paintRec );
				} );
			} );
		}
		api( 'recurring' ).then( paintRec );
	}

	/* ---- Settings: theme + language ---- */
	var setRoot = document.getElementById( 'cb-settings' );
	if ( setRoot ) {
		var stored = 'system';
		try { stored = localStorage.getItem( 'cb_theme' ) || 'system'; } catch ( e ) {}
		var themeRadio = setRoot.querySelector( 'input[name="cb-theme"][value="' + stored + '"]' );
		if ( themeRadio ) { themeRadio.checked = true; }
		Array.prototype.forEach.call( setRoot.querySelectorAll( 'input[name="cb-theme"]' ), function ( r ) {
			r.addEventListener( 'change', function () {
				var v = r.value;
				try { localStorage.setItem( 'cb_theme', v ); } catch ( e ) {}
				if ( v === 'system' ) { document.documentElement.removeAttribute( 'data-theme' ); }
				else { document.documentElement.setAttribute( 'data-theme', v ); }
			} );
		} );
	}
} )();
