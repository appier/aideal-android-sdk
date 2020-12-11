(function() {
  window.injectCouponCode = injectCouponCode;
  window.getOffsetWidth = getOffsetWidth;

  setViewport();
  document.addEventListener('click', onClick, false);

  function setViewport() {
    var meta = document.createElement('meta');
    meta.setAttribute('name', 'viewport');
    meta.setAttribute('content', 'width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no');
    var head = document.getElementsByTagName('head')[0];
    if (!head) return;
    head.appendChild(meta);
  }

  function onClick(event) {
    event.preventDefault();
    var target = event.target;
    var copyCode = target.hasAttribute('aid-copycode');
    if (copyCode) {
      AiDeal.copyCode();
    }
    var close = target.hasAttribute('aid-close');
    if (close) {
      AiDeal.close();
    }
  }

  function injectCouponCode(couponCode) {
    var elem = document.querySelector('.zc_code_insert_code');
    if (!elem) return;
    elem.innerText = couponCode;
  };

  function getOffsetWidth() {
    return document.querySelector('#zc-plugincontainer .zc_modal').offsetWidth;
  }
})();
