import { useState, useEffect } from 'react';

function useUserAgent() {
  const [userAgent, setUserAgent] = useState({
    isMobile: false,
    isDesktop: false,
    isAndroid: false,
    isIOS: false,
    isWindows: false,
    isMac: false,
    isLinux: false,
    browser: 'unknown',
    os: 'unknown',
  });

  useEffect(() => {
    const ua = navigator.userAgent;
    const newAgent = {
      isMobile: /Mobi|Android|iPhone|iPad|iPod|Windows Phone/i.test(ua),
      isDesktop: !/Mobi|Android|iPhone|iPad|iPod|Windows Phone/i.test(ua),
      isAndroid: /Android/i.test(ua),
      isIOS: /iPhone|iPad|iPod/i.test(ua),
      isWindows: /Windows/i.test(ua),
      isMac: /Macintosh|Mac OS X/i.test(ua),
      isLinux: /Linux/i.test(ua),
      browser: 'unknown',
      os: 'unknown',
    };

    // Detect OS
    if (newAgent.isWindows) newAgent.os = 'Windows';
    else if (newAgent.isMac) newAgent.os = 'macOS';
    else if (newAgent.isAndroid) newAgent.os = 'Android';
    else if (newAgent.isIOS) newAgent.os = 'iOS';
    else if (newAgent.isLinux) newAgent.os = 'Linux';
    else if (/CrOS/.test(ua)) newAgent.os = 'Chrome OS';
    else if (/Firefox/.test(ua)) newAgent.os = 'Firefox OS';

    // Detect Browser
    if (/Edg/.test(ua)) newAgent.browser = 'Edge';
    else if (/Chrome/.test(ua) && !/Edg/.test(ua)) newAgent.browser = 'Chrome';
    else if (/Firefox/.test(ua)) newAgent.browser = 'Firefox';
    else if (/Safari/.test(ua) && !/Chrome/.test(ua)) newAgent.browser = 'Safari';
    else if (/MSIE|Trident/.test(ua)) newAgent.browser = 'IE';

    setUserAgent(newAgent);
  }, []);

  return userAgent;
}

export default useUserAgent;
