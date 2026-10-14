/* Keep React-rendered WooCommerce Cart/Checkout Blocks visually consistent
 * with the Persian Carmilla storefront without changing form-control values. */
( function () {
	'use strict';

	var latin = '0123456789';
	var persian = '۰۱۲۳۴۵۶۷۸۹';
	var blocked = 'SCRIPT,STYLE,TEXTAREA,INPUT,SELECT,OPTION';

	function localizeText( text ) {
		if ( ! /[0-9]/.test( text ) ) {
			return text;
		}
		return text.replace( /[0-9]/g, function ( digit ) {
			return persian.charAt( latin.indexOf( digit ) );
		} ).replace( /,/g, '٬' ).replace( /\./g, '٫' );
	}

	function visit( node ) {
		if ( node.nodeType === Node.TEXT_NODE ) {
			if ( node.parentElement && blocked.indexOf( node.parentElement.tagName ) === -1 ) {
				node.nodeValue = localizeText( node.nodeValue );
			}
			return;
		}
		if ( node.nodeType !== Node.ELEMENT_NODE || blocked.indexOf( node.tagName ) !== -1 ) {
			return;
		}
		Array.prototype.forEach.call( node.childNodes, visit );
	}

	function apply() {
		document.querySelectorAll( '.wp-block-woocommerce-cart, .wp-block-woocommerce-checkout' ).forEach( visit );
	}

	if ( document.readyState === 'loading' ) {
		document.addEventListener( 'DOMContentLoaded', apply, { once: true } );
	} else {
		apply();
	}

	new MutationObserver( apply ).observe( document.documentElement, { childList: true, subtree: true } );
}() );
