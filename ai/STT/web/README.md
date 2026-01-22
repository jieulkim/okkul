# Web STT 페이지 (브라우저 Web Speech API)

이 폴더는 **서버 없이(또는 로컬 서버로) 브라우저에서 영어 STT**를 수행하는 단일 페이지입니다.

- 위: 한글 텍스트 입력(말할 내용/주제 메모용)
- 아래: **영어 말하기 시작** 버튼 → 마이크로 영어를 말하면 **영어 텍스트(STT 결과)** 출력

> 이 방식은 OpenAI API를 사용하지 않습니다. (브라우저의 Web Speech API 사용)

---

## 실행 방법 (권장: localhost)

PowerShell:

```powershell
cd C:\Users\SSAFY\Desktop\STT\web
python -m http.server 8000
```

그 다음 브라우저에서:

- `http://localhost:8000`

마이크 권한을 허용하면 동작합니다.

---

## 주의

- Chrome/Edge 권장
- HTTPS 또는 localhost에서 마이크 권한이 정상 동작합니다.

