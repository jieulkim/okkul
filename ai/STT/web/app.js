(() => {
  const $ = (id) => document.getElementById(id);

  const koreanInput = $("koreanInput");
  const englishOutput = $("englishOutput");
  const toggleBtn = $("toggleBtn");
  const clearBtn = $("clearBtn");
  const copyBtn = $("copyBtn");
  const supportPill = $("supportPill");
  const statePill = $("statePill");
  const helpBox = $("helpBox");

  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  const supported = !!SpeechRecognition;

  let recognition = null;
  let listening = false;
  let confirmedText = ""; // 확정된 텍스트만 저장

  function setState(text) {
    statePill.textContent = `상태: ${text}`;
  }

  function setSupport(text, ok) {
    supportPill.textContent = text;
    supportPill.style.borderColor = ok ? "rgba(124, 92, 255, 0.55)" : "rgba(255, 92, 124, 0.55)";
  }

  function updateOutput(finalText = "", interimText = "") {
    // 확정된 텍스트 추가
    if (finalText.trim()) {
      confirmedText += (confirmedText ? " " : "") + finalText.trim();
    }

    // 화면에 표시: 확정 텍스트 + 중간 텍스트 (실시간 업데이트)
    let displayText = confirmedText;
    if (interimText.trim()) {
      displayText += (displayText ? " " : "") + interimText.trim();
    }
    
    // 화면 업데이트
    if (englishOutput) {
      englishOutput.value = displayText;
      englishOutput.scrollTop = englishOutput.scrollHeight;
    }
  }

  function initRecognition() {
    recognition = new SpeechRecognition();

    // English STT
    recognition.lang = "en-US";

    // Streaming-ish behavior: interim results + continuous (when available)
    recognition.interimResults = true;
    recognition.continuous = true;

    recognition.onstart = () => {
      listening = true;
      toggleBtn.textContent = "영어 말하기 중지";
      toggleBtn.classList.add("danger");
      setState("듣는 중...");
    };

    recognition.onend = () => {
      listening = false;
      toggleBtn.textContent = "영어 말하기 시작";
      toggleBtn.classList.remove("danger");
      setState("대기");
    };

    recognition.onerror = (e) => {
      setState(`오류: ${e.error}`);
      helpBox.hidden = false;
    };

    recognition.onresult = (event) => {
      // 실시간 스트리밍: 확정된 텍스트와 중간 텍스트를 모두 화면에 표시
      let finalText = "";
      let interimText = "";

      // 모든 결과를 순회하면서 확정/중간 결과 분리
      for (let i = 0; i < event.results.length; i++) {
        const res = event.results[i];
        const text = res[0]?.transcript ?? "";
        if (res.isFinal) {
          finalText += (finalText ? " " : "") + text;
        } else {
          interimText += (interimText ? " " : "") + text;
        }
      }

      // 디버깅: 콘솔에 출력 (개발자 도구에서 확인 가능)
      if (finalText || interimText) {
        console.log("Final:", finalText, "Interim:", interimText);
      }

      // 화면에 실시간 업데이트 (확정 + 중간 결과 모두 표시)
      updateOutput(finalText, interimText);

      // 제목에도 중간 결과 표시 (선택사항)
      document.title = interimText.trim() ? `… ${interimText.trim()}` : "Speak English (STT)";
    };
  }

  // UI actions
  toggleBtn.addEventListener("click", async () => {
    if (!supported) return;

    if (!recognition) initRecognition();

    // Best-effort mic permission prompt
    try {
      if (navigator.mediaDevices?.getUserMedia) {
        await navigator.mediaDevices.getUserMedia({ audio: true });
      }
    } catch (e) {
      helpBox.hidden = false;
      setState("마이크 권한 필요");
      return;
    }

    if (listening) recognition.stop();
    else {
      helpBox.hidden = true;
      recognition.start();
    }
  });

  clearBtn.addEventListener("click", () => {
    confirmedText = "";
    englishOutput.value = "";
    document.title = "Speak English (STT)";
  });

  copyBtn.addEventListener("click", async () => {
    const text = englishOutput.value.trim();
    if (!text) return;
    try {
      await navigator.clipboard.writeText(text);
      setState("복사 완료");
      setTimeout(() => setState("대기"), 900);
    } catch (e) {
      setState("복사 실패");
    }
  });

  // Nice-to-have: if user pasted Korean prompt, keep focus behavior simple
  koreanInput.addEventListener("keydown", (e) => {
    if ((e.ctrlKey || e.metaKey) && e.key === "Enter") {
      toggleBtn.click();
    }
  });

  // Bootstrap
  if (!supported) {
    setSupport("Web Speech API 미지원 (Chrome/Edge 권장)", false);
    setState("지원되지 않음");
    helpBox.hidden = false;
    toggleBtn.disabled = true;
    toggleBtn.style.opacity = 0.6;
  } else {
    setSupport("Web Speech API 지원됨", true);
    setState("대기");
  }
})();

