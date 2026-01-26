# STT (OpenAI Whisper - 오픈소스 로컬 실행)

Windows에서 **Whisper(openai-whisper)** 를 로컬로 설치하고 바로 음성→텍스트(STT)를 실행하는 최소 예제입니다.  
Whisper는 내부적으로 **FFmpeg** 를 사용하므로 **FFmpeg 설치 + PATH 등록**이 필수입니다.

---

## 1) 준비물

- **Python 3.10~3.12 권장**
- **FFmpeg 설치 (PATH 등록 필수)**
- (선택) **NVIDIA GPU + CUDA 지원 Torch**: 있으면 훨씬 빠릅니다.

---

## 2) 가상환경 생성/활성화 (권장)

PowerShell 기준:

```powershell
cd C:\Users\SSAFY\Desktop\STT
python -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install --upgrade pip
```

---

## 3) Whisper 설치

```powershell
pip install -r requirements.txt
```

> `openai-whisper`는 Torch를 의존성으로 가져오긴 하지만, **GPU(CUDA) 지원 버전**을 쓰려면 보통 Torch를 별도로 맞춰 설치하는 걸 권장합니다(아래 참고).
>
> 이 프로젝트는 Windows에서 `numba`/`opencv` 등과의 호환을 위해 **NumPy를 `numpy<2.3`으로 고정**합니다.

---

## 4) FFmpeg 설치 (Windows)

### 방법 A: winget (가장 간단)

```powershell
winget install --id Gyan.FFmpeg -e
```

설치 후 새 터미널을 열고 다음이 동작하는지 확인하세요:

```powershell
ffmpeg -version
```

### 방법 B: 직접 설치

- FFmpeg zip 다운로드 → 압축 해제
- `bin` 폴더(예: `C:\ffmpeg\bin`)를 **시스템 PATH** 또는 **사용자 PATH**에 추가
- 새 터미널에서 `ffmpeg -version` 확인

---

## 5) (선택) GPU 가속(Torch CUDA) 설치

Whisper는 PyTorch를 사용합니다. GPU를 쓰려면 **CUDA 지원 Torch**가 필요합니다.

- 현재 설치된 CUDA/드라이버에 맞는 설치 명령은 PyTorch 공식 페이지를 참고하세요:  
  `https://pytorch.org/get-started/locally/`

설치가 됐는지 확인:

```powershell
python -c "import torch; print('torch', torch.__version__); print('cuda available:', torch.cuda.is_available())"
```

---

## 6) 실행

### 기본 실행 (자동으로 GPU 있으면 GPU, 없으면 CPU)

```powershell
python transcribe.py --input "sample.mp3"
```

### 모델 선택

```powershell
python transcribe.py --input "sample.mp3" --model medium
```

### 언어 힌트(정확도/속도 도움)

```powershell
python transcribe.py --input "sample.mp3" --language ko
```

### 출력 형식(txt/srt/vtt/json)

```powershell
python transcribe.py --input "sample.mp3" --output-dir outputs --formats txt srt vtt json
```

---

## 7) 자주 나는 오류

### `ffmpeg`를 찾을 수 없다고 나올 때

- `ffmpeg -version`이 터미널에서 안 되면 PATH가 안 잡힌 상태입니다.
- FFmpeg 설치 후 **새 터미널**에서 다시 시도하세요.
- (이 프로젝트의 `transcribe.py`는 winget 설치 경로를 자동 탐색해 **재시작 없이도** 잡히게 시도합니다.)

### CUDA가 있는데도 CPU로만 도는 것 같을 때

- `torch.cuda.is_available()`가 `False`면 Torch가 CUDA 빌드가 아닙니다.
- PyTorch 공식 페이지의 CUDA 설치 명령으로 Torch를 재설치하세요.

### `OMP: Error #15 ... libomp.dll ... libiomp5md.dll ...` 오류가 날 때

- Windows에서 torch/numpy 조합에 따라 발생할 수 있는 OpenMP 런타임 충돌입니다.
- 이 프로젝트의 `transcribe.py`는 기본으로 `KMP_DUPLICATE_LIB_OK=TRUE` 우회를 적용했습니다.

---

## 파일

- `requirements.txt`: Python 의존성
- `transcribe.py`: Whisper STT 실행 스크립트

