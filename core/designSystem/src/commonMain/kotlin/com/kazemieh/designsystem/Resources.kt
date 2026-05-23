package com.kazemieh.designsystem

import com.kazemieh.common.*
import com.kazemieh.common.Res as CommonRes
import com.kazemieh.designsystem.Res as DesignRes

object Resources {
    object Icon {
        val Plus = DesignRes.drawable.plus
        val Minus = DesignRes.drawable.minus
        val SignIn = DesignRes.drawable.log_in
        val SignOut = DesignRes.drawable.log_out
        val Unlock = DesignRes.drawable.unlock
        val Search = DesignRes.drawable.search
        val Person = DesignRes.drawable.user
        val Checkmark = DesignRes.drawable.check
        val Edit = DesignRes.drawable.edit
        val Menu = DesignRes.drawable.menu
        val BackArrow = DesignRes.drawable.back_arrow
        val RightArrow = DesignRes.drawable.right_arrow
        val Home = DesignRes.drawable.home
        val ShoppingCart = DesignRes.drawable.shopping_cart
        val Categories = DesignRes.drawable.grid
        val Dollar = DesignRes.drawable.dollar
        val MapPin = DesignRes.drawable.map_pin
        val Close = DesignRes.drawable.close
        val Book = DesignRes.drawable.book
        val VerticalMenu = DesignRes.drawable.vertical_menu
        val Delete = DesignRes.drawable.delete
        val Warning = DesignRes.drawable.warning
        val Weight = DesignRes.drawable.weight
        val Settings = DesignRes.drawable.settings
    }

    object Image {
        val AppLogo = DesignRes.drawable.ic_carmilla
        val ShoppingCart = DesignRes.drawable.shopping_cart_image
        val Checkmark = DesignRes.drawable.checkmark_image
        val Cat = DesignRes.drawable.cat
        val GoogleLogo = DesignRes.drawable.google_logo
        val PaypalLogo = DesignRes.drawable.paypal_logo
    }

    object Flag {
        val India = DesignRes.drawable.india
        val Usa = DesignRes.drawable.usa
        val Serbia = DesignRes.drawable.serbia
    }

    object String {
        val MyProfile = CommonRes.string.my_profile
        val MyAddresses = CommonRes.string.my_addresses
        val Settings = CommonRes.string.settings
        val Language = CommonRes.string.language
        val English = CommonRes.string.english
        val Persian = CommonRes.string.persian
        val Theme = CommonRes.string.theme
        val Light = CommonRes.string.light
        val Dark = CommonRes.string.dark
        val SystemDefault = CommonRes.string.system_default
        val NoAddressesFound = CommonRes.string.no_addresses_found
        val Default = CommonRes.string.default_label
        val PostalCodeLabel = CommonRes.string.postal_code_label
        val SetAsDefault = CommonRes.string.set_as_default
        val UpdateProfile = CommonRes.string.update_profile
        val Saving = CommonRes.string.saving
        val Oops = CommonRes.string.oops
        val BackArrowDesc = CommonRes.string.back_arrow_desc
        val AddAddress = CommonRes.string.add_address
        val Edit = CommonRes.string.edit
        val Delete = CommonRes.string.delete
        val FeaturedProducts = CommonRes.string.featured_products
        val AllProducts = CommonRes.string.all_products
        val TotalLabel = CommonRes.string.total_label
        val Checkout = CommonRes.string.checkout
        val AddNewAddress = CommonRes.string.add_new_address
        val SaveAddress = CommonRes.string.save_address
        val Copy = CommonRes.string.copy
        val AdminPanel = CommonRes.string.admin_panel
        val SearchHere = CommonRes.string.search_here
        val OutOfStock = CommonRes.string.out_of_stock
        val Details = CommonRes.string.details
        val Variants = CommonRes.string.variants
        val AddToCart = CommonRes.string.add_to_cart
        val SelectAddress = CommonRes.string.select_address
        val AddYourFirstAddress = CommonRes.string.add_your_first_address
        val PayWithPayPal = CommonRes.string.pay_with_paypal
        val PayOnDelivery = CommonRes.string.pay_on_delivery
        val Login = CommonRes.string.login

        // Error/Result handling
        val ErrorParsingData = CommonRes.string.error_parsing_data
        val UnknownError = CommonRes.string.unknown_error
        val GoBack = CommonRes.string.go_back
        val Success = CommonRes.string.success
        val PurchaseOnTheWay = CommonRes.string.purchase_on_the_way
        val NothingHere = CommonRes.string.nothing_here
        val EmptyProductList = CommonRes.string.empty_product_list
        val NoProductInCategory = CommonRes.string.no_product_in_category
        val AddressAddedSuccessfully = CommonRes.string.address_added_successfully
        val PaypalNotImplemented = CommonRes.string.paypal_not_implemented
        val CartIsEmptyError = CommonRes.string.cart_is_empty_error
        val SelectAddressError = CommonRes.string.select_address_error
        val SomethingWentWrong = CommonRes.string.something_went_wrong
        val Loading = CommonRes.string.loading
        val ProfileNotFound = CommonRes.string.profile_not_found

        // Admin/Management
        val SelectCategory = CommonRes.string.select_category
        val SelectColor = CommonRes.string.select_color
        val SelectSize = CommonRes.string.select_size
        val ManageOrders = CommonRes.string.manage_orders
        val OrderIdPrefix = CommonRes.string.order_id_prefix
        val DeleteProduct = CommonRes.string.delete_product
        val AddVariant = CommonRes.string.add_variant
        val EditVariant = CommonRes.string.edit_variant
        val AddNewVariant = CommonRes.string.add_new_variant
        val Sku = CommonRes.string.sku
        val Price = CommonRes.string.price
        val Active = CommonRes.string.active
        val Inactive = CommonRes.string.inactive
        val DeleteVariant = CommonRes.string.delete_variant
        val Update = CommonRes.string.update
        val Cancel = CommonRes.string.cancel
        val InitialStock = CommonRes.string.initial_stock
        val Add = CommonRes.string.add
        val CreateCategory = CommonRes.string.create_category
        val Name = CommonRes.string.name
        val Slug = CommonRes.string.slug
        val Create = CommonRes.string.create
        val AddNew = CommonRes.string.add_new
        val CreateSize = CommonRes.string.create_size
        val SortOrder = CommonRes.string.sort_order
        val CreateColor = CommonRes.string.create_color
        val HexCodeOptional = CommonRes.string.hex_code_optional
        val HexCodePlaceholder = CommonRes.string.hex_code_placeholder
        val ProductIdActiveFormat = CommonRes.string.product_id_active_format
        val NewProduct = CommonRes.string.new_product
        val EditProduct = CommonRes.string.edit_product
        val ProductTitlePlaceholder = CommonRes.string.product_title_placeholder
        val BasePricePlaceholder = CommonRes.string.base_price_placeholder
        val AddProduct = CommonRes.string.add_product
        val UpdateProduct = CommonRes.string.update_product
        val StatusUpdatedSuccessfully = CommonRes.string.status_updated_successfully
        val OrderDetail = CommonRes.string.order_detail
        val CustomerInformation = CommonRes.string.customer_information
        val ShippingAddress = CommonRes.string.shipping_address
        val UpdateStatus = CommonRes.string.update_status
        val NoOrdersFound = CommonRes.string.no_orders_found
        val PleaseAddAtLeastOneVariant = CommonRes.string.please_add_at_least_one_variant
        val ProductSavedSuccessfully = CommonRes.string.product_saved_successfully
        val ProductDeletedSuccessfully = CommonRes.string.product_deleted_successfully
        val ImageDeleted = CommonRes.string.image_deleted
        val VariantAdded = CommonRes.string.variant_added
        val VariantUpdated = CommonRes.string.variant_updated
        val VariantDeleted = CommonRes.string.variant_deleted
        val CategoryCreated = CommonRes.string.category_created
        val CategoryDeleted = CommonRes.string.category_deleted
        val SizeCreated = CommonRes.string.size_created
        val SizeUpdated = CommonRes.string.size_updated
        val SizeDeleted = CommonRes.string.size_deleted
        val ColorCreated = CommonRes.string.color_created
        val ColorUpdated = CommonRes.string.color_updated
        val ColorDeleted = CommonRes.string.color_deleted
        val ImageUploaded = CommonRes.string.image_uploaded
        val ImageSelected = CommonRes.string.image_selected
        val ProductAddedToCart = CommonRes.string.product_added_to_cart
        val CheckoutWithQty = CommonRes.string.checkout_with_qty
        val ProductImageDesc = CommonRes.string.product_image_desc

        val ItemsLabel = CommonRes.string.items_label
        val SubtotalLabel = CommonRes.string.subtotal_label
        val ShippingLabel = CommonRes.string.shipping_label
        val TotalLabelSimple = CommonRes.string.total_label_simple
        val EmailLabelFormat = CommonRes.string.email_label_format
        val UserIdLabelFormat = CommonRes.string.user_id_label_format
        val AllLabel = CommonRes.string.all_label
        val PriceFormat = CommonRes.string.price_format
        val VariantFormat = CommonRes.string.variant_format
        val AddressFormat = CommonRes.string.address_format
        val CityProvinceFormat = CommonRes.string.city_province_format
        val QtyXPriceFormat = CommonRes.string.qty_x_price_format
        val PriceRangeFormat = CommonRes.string.price_range_format
        val StockReservedFormat = CommonRes.string.stock_reserved_format

        // Auth
        val ForgotPassword = CommonRes.string.forgot_password
        val CreateAccount = CommonRes.string.create_account
        val BackToLogin = CommonRes.string.back_to_login
        val AlreadyHaveAccount = CommonRes.string.already_have_account
        val EmailHint = CommonRes.string.email_hint
        val PasswordHint = CommonRes.string.password_hint
        val SendResetLink = CommonRes.string.send_reset_link
        val PasswordTooShort = CommonRes.string.password_too_short
        val InvalidEmail = CommonRes.string.invalid_email
        val EmailEmpty = CommonRes.string.email_empty

        // Home/Drawer
        val Carmilla = CommonRes.string.carmilla
        val CarmillaSlogan = CommonRes.string.carmilla_slogan
        val Home = CommonRes.string.home
        val Cart = CommonRes.string.cart
        val Categories = CommonRes.string.categories
        val Profile = CommonRes.string.profile
        val Blog = CommonRes.string.blog
        val Locations = CommonRes.string.locations
        val ContactUs = CommonRes.string.contact_us
        val Address = CommonRes.string.address
        val PhoneNumber = CommonRes.string.phone_number
        val SignOut = CommonRes.string.sign_out
        val CartIsEmpty = CommonRes.string.cart_is_empty
        val CartIsEmptySubtitle = CommonRes.string.cart_is_empty_subtitle
        val CategoriesIsEmpty = CommonRes.string.categories_is_empty
        val CategoriesIsEmptySubtitle = CommonRes.string.categories_is_empty_subtitle
        val SearchInCategoryFormat = CommonRes.string.search_in_category_format

        // Content Descriptions
        val BackDesc = CommonRes.string.back_desc
        val CloseDesc = CommonRes.string.close_desc
        val SearchDesc = CommonRes.string.search_desc
        val MenuDesc = CommonRes.string.menu_desc
        val ProductThumbnailDesc = CommonRes.string.product_thumbnail_desc
        val DrawerItemDesc = CommonRes.string.drawer_item_desc
        val AppLogoDesc = CommonRes.string.app_logo_desc
        val BottomBarDesc = CommonRes.string.bottom_bar_desc
        val MinusDesc = CommonRes.string.minus_desc
        val PlusDesc = CommonRes.string.plus_desc
        val CloseIconDesc = CommonRes.string.close_icon_desc
        val BackArrowIconDesc = CommonRes.string.back_arrow_icon_desc
        val OrdersIconDesc = CommonRes.string.orders_icon_desc
        val SearchIconDesc = CommonRes.string.search_icon_desc
        val AddIconDesc = CommonRes.string.add_icon_desc
        val VerticalMenuIconDesc = CommonRes.string.vertical_menu_icon_desc
        val DeleteIconDesc = CommonRes.string.delete_icon_desc
        val MessageBarIconDesc = CommonRes.string.message_bar_icon_desc
        val TextFieldIconDesc = CommonRes.string.text_field_icon_desc
        val ButtonIconDesc = CommonRes.string.button_icon_desc
        val InfoCardImageDesc = CommonRes.string.info_card_image_desc

        // Form Placeholders
        val FirstNamePlaceholder = CommonRes.string.first_name_placeholder
        val LastNamePlaceholder = CommonRes.string.last_name_placeholder
        val PhoneNumberPlaceholder = CommonRes.string.phone_number_placeholder
        val ReceiverNamePlaceholder = CommonRes.string.receiver_name_placeholder
        val ReceiverPhonePlaceholder = CommonRes.string.receiver_phone_placeholder
        val ProvincePlaceholder = CommonRes.string.province_placeholder
        val CityPlaceholder = CommonRes.string.city_placeholder
        val AddressLine1Placeholder = CommonRes.string.address_line1_placeholder
        val AddressLine2OptionalPlaceholder = CommonRes.string.address_line2_optional_placeholder
        val PostalCodeOptionalPlaceholder = CommonRes.string.postal_code_optional_placeholder
    }
}
