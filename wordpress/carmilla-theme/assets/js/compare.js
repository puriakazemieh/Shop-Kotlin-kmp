/* Product comparison: localStorage list + toggles + full-width table. */
( function () {
	'use strict';
	var cfg = window.CarmillaData || {};
	var KEY = 'cb_compare';
	var MAX = 4;

	function getList() {
		try { return JSON.parse( localStorage.getItem( KEY ) || '[]' ); } catch ( e ) { return []; }
	}
	function setList( ids ) { localStorage.setItem( KEY, JSON.stringify( ids ) ); }

	function esc( s ) { var d = document.createElement( 'div' ); d.textContent = s == null ? '' : s; return d.innerHTML; }

	var toggles = Array.prototype.slice.call( document.querySelectorAll( '.cb-compare-toggle' ) );
	var bar = document.getElementById( 'cb-compare-bar' );
	var countEl = document.getElementById( 'cb-compare-count' );
	var clearBtn = document.getElementById( 'cb-compare-clear' );
	var table = document.getElementById( 'cb-compare' );

	function syncToggles() {
		var ids = getList();
		toggles.forEach( function ( b ) {
			var on = ids.indexOf( parseInt( b.getAttribute( 'data-id' ), 10 ) ) !== -1;
			b.setAttribute( 'aria-pressed', on ? 'true' : 'false' );
			b.classList.toggle( 'is-on', on );
		} );
		if ( bar ) {
			if ( ids.length ) {
				bar.hidden = false;
				countEl.textContent = ids.length + ' مورد برای مقایسه';
			} else {
				bar.hidden = true;
			}
		}
	}

	toggles.forEach( function ( b ) {
		b.addEventListener( 'click', function () {
			var id = parseInt( b.getAttribute( 'data-id' ), 10 );
			var ids = getList();
			var i = ids.indexOf( id );
			if ( i === -1 ) {
				if ( ids.length >= MAX ) { alert( 'حداکثر ' + MAX + ' محصول قابل مقایسه است.' ); return; }
				ids.push( id );
			} else {
				ids.splice( i, 1 );
			}
			setList( ids );
			syncToggles();
			if ( table ) { renderTable(); }
		} );
	} );

	if ( clearBtn ) {
		clearBtn.addEventListener( 'click', function () { setList( [] ); syncToggles(); if ( table ) { renderTable(); } } );
	}

	function renderTable() {
		var ids = getList();
		if ( ! ids.length ) {
			table.innerHTML = '<p class="t-body t-muted">' + esc( table.getAttribute( 'data-empty' ) ) + '</p>';
			return;
		}
		fetch( cfg.restUrl + 'compare?ids=' + ids.join( ',' ), { credentials: 'same-origin' } )
			.then( function ( r ) { return r.json(); } )
			.then( function ( items ) {
				if ( ! items || ! items.length ) { table.innerHTML = ''; return; }
				var keys = [];
				items.forEach( function ( it ) {
					Object.keys( it.attributes || {} ).forEach( function ( k ) { if ( keys.indexOf( k ) === -1 ) { keys.push( k ); } } );
				} );
				var html = '<div class="cb-compare__scroll"><table class="cb-compare__t"><thead><tr><th></th>';
				items.forEach( function ( it ) {
					html += '<th><a href="' + esc( it.permalink ) + '"><img src="' + esc( it.image ) + '" alt="" class="cb-compare__img"><span class="t-title-sm">' + esc( it.name ) + '</span></a></th>';
				} );
				html += '</tr></thead><tbody>';
				html += row( 'قیمت', items.map( function ( it ) { return it.priceHtml; } ), true );
				html += row( 'امتیاز', items.map( function ( it ) { return it.rating ? ( '★ ' + it.rating ) : '—'; } ) );
				html += row( 'موجودی', items.map( function ( it ) { return it.inStock ? 'موجود' : 'ناموجود'; } ) );
				html += row( 'دسته‌بندی', items.map( function ( it ) { return it.category || '—'; } ) );
				keys.forEach( function ( k ) {
					html += row( k, items.map( function ( it ) { return ( it.attributes && it.attributes[ k ] ) || '—'; } ) );
				} );
				html += '</tbody></table></div>';
				table.innerHTML = html;
			} );
	}

	function row( label, values, isHtml ) {
		var tds = values.map( function ( v ) {
			return '<td>' + ( isHtml ? v : esc( v ) ) + '</td>';
		} ).join( '' );
		return '<tr><th scope="row">' + esc( label ) + '</th>' + tds + '</tr>';
	}

	syncToggles();
	if ( table ) { renderTable(); }
} )();
