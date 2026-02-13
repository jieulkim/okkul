import argparse
import json
import os
import shutil
from pathlib import Path


def _ensure_ffmpeg() -> None:
    if shutil.which("ffmpeg") is not None:
        return

    # Windows에서 winget으로 설치한 직후, "새 터미널"을 열기 전까지 PATH가 반영되지 않는 경우가 흔합니다.
    # 가능한 대표 경로에서 ffmpeg.exe를 찾아 현재 프로세스 PATH에 임시로 추가해 재시작 없이 동작하게 합니다.
    candidates: list[Path] = []

    localappdata = os.environ.get("LOCALAPPDATA")
    if localappdata:
        # e.g. C:\Users\<you>\AppData\Local\Microsoft\WinGet\Packages\...\...\bin\ffmpeg.exe
        winget_pkgs = Path(localappdata) / "Microsoft" / "WinGet" / "Packages"
        if winget_pkgs.exists():
            try:
                for p in winget_pkgs.rglob("ffmpeg.exe"):
                    candidates.append(p)
                    if len(candidates) >= 20:
                        break
            except OSError:
                # 일부 경로 접근 실패는 무시
                pass

    # 흔한 수동 설치 위치도 함께 시도
    for base in ("C:\\ffmpeg\\bin\\ffmpeg.exe", "C:\\Program Files\\ffmpeg\\bin\\ffmpeg.exe"):
        candidates.append(Path(base))

    for exe in candidates:
        if exe.exists():
            os.environ["PATH"] = str(exe.parent) + os.pathsep + os.environ.get("PATH", "")
            if shutil.which("ffmpeg") is not None:
                return

    raise SystemExit(
        "FFmpeg를 찾을 수 없습니다. 먼저 FFmpeg를 설치하고 PATH에 등록하세요.\n"
        "- 확인: ffmpeg -version\n"
        "- winget: winget install --id Gyan.FFmpeg -e\n"
        "- 설치 후에도 안 되면: 터미널/IDE를 완전히 재시작해 PATH를 갱신하세요.\n"
    )


def _write_txt(path: Path, text: str) -> None:
    path.write_text(text.strip() + "\n", encoding="utf-8")


def _write_json(path: Path, data: dict) -> None:
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def _write_srt(path: Path, segments: list[dict]) -> None:
    def fmt(ts: float) -> str:
        ms = int(round(ts * 1000))
        h = ms // 3_600_000
        ms %= 3_600_000
        m = ms // 60_000
        ms %= 60_000
        s = ms // 1000
        ms %= 1000
        return f"{h:02}:{m:02}:{s:02},{ms:03}"

    lines: list[str] = []
    for i, seg in enumerate(segments, start=1):
        lines.append(str(i))
        lines.append(f"{fmt(float(seg['start']))} --> {fmt(float(seg['end']))}")
        lines.append(str(seg.get("text", "")).strip())
        lines.append("")
    path.write_text("\n".join(lines).rstrip() + "\n", encoding="utf-8")


def _write_vtt(path: Path, segments: list[dict]) -> None:
    def fmt(ts: float) -> str:
        ms = int(round(ts * 1000))
        h = ms // 3_600_000
        ms %= 3_600_000
        m = ms // 60_000
        ms %= 60_000
        s = ms // 1000
        ms %= 1000
        return f"{h:02}:{m:02}:{s:02}.{ms:03}"

    lines: list[str] = ["WEBVTT", ""]
    for seg in segments:
        lines.append(f"{fmt(float(seg['start']))} --> {fmt(float(seg['end']))}")
        lines.append(str(seg.get("text", "")).strip())
        lines.append("")
    path.write_text("\n".join(lines).rstrip() + "\n", encoding="utf-8")


def main() -> int:
    # Windows에서 torch/numba/numpy 조합에 따라 OpenMP 런타임 충돌이 발생할 수 있습니다.
    # (libomp.dll vs libiomp5md.dll) — 아래는 널리 쓰이는 우회 설정입니다.
    os.environ.setdefault("KMP_DUPLICATE_LIB_OK", "TRUE")

    parser = argparse.ArgumentParser(description="Local STT using openai-whisper (requires FFmpeg).")
    input_group = parser.add_mutually_exclusive_group(required=False)
    input_group.add_argument("--input", help="Audio/video file path (mp3/wav/mp4/etc)")
    input_group.add_argument(
        "--input-auto",
        action="store_true",
        help="Auto-pick an audio/video file from current directory (useful when non-ASCII paths break in shell).",
    )
    input_group.add_argument(
        "--input-index",
        type=int,
        default=None,
        help="Pick Nth detected media file from current directory (0-based).",
    )
    input_group.add_argument(
        "--input-name",
        default=None,
        help="Pick a file from current directory by name substring (case-insensitive). Useful for non-ASCII names.",
    )
    parser.add_argument("--model", default="small", help="tiny/base/small/medium/large (default: small)")
    parser.add_argument("--language", default=None, help="Language code hint, e.g. ko, en (optional)")
    parser.add_argument("--task", default="transcribe", choices=["transcribe", "translate"], help="transcribe or translate")
    parser.add_argument("--device", default=None, help="cpu or cuda (optional; auto if omitted)")
    parser.add_argument("--output-dir", default="outputs", help="Output directory (default: outputs)")
    parser.add_argument(
        "--formats",
        nargs="+",
        default=["txt"],
        choices=["txt", "srt", "vtt", "json"],
        help="Output formats (default: txt)",
    )
    args = parser.parse_args()

    _ensure_ffmpeg()

    def find_media_candidates(search_dir: Path) -> list[Path]:
        exts = {
            ".mp3",
            ".wav",
            ".m4a",
            ".mp4",
            ".mkv",
            ".webm",
            ".ogg",
            ".flac",
            ".aac",
            ".wma",
        }
        return sorted([p for p in search_dir.iterdir() if p.is_file() and p.suffix.lower() in exts])

    cwd = Path.cwd()
    if args.input:
        input_path = Path(args.input).expanduser().resolve()
    else:
        candidates = find_media_candidates(cwd)
        if not candidates:
            raise SystemExit(
                "현재 폴더에서 오디오/비디오 파일을 찾지 못했습니다.\n"
                "파일을 이 폴더로 옮기거나, --input 으로 경로를 직접 지정하세요."
            )
        if args.input_name is not None:
            needle = str(args.input_name).casefold()
            matches = [p for p in candidates if needle in p.name.casefold()]
            if not matches:
                listing = "\n".join([f"- {p.name}" for p in candidates])
                raise SystemExit(
                    f"--input-name '{args.input_name}' 에 해당하는 파일을 찾지 못했습니다.\n"
                    f"현재 폴더 후보:\n{listing}"
                )
            if len(matches) > 1:
                listing = "\n".join([f"[{i}] {p.name}" for i, p in enumerate(matches)])
                raise SystemExit(
                    f"--input-name '{args.input_name}' 결과가 여러 개입니다. 더 구체적으로 지정하세요:\n{listing}"
                )
            input_path = matches[0].resolve()
        elif args.input_index is not None:
            if args.input_index < 0 or args.input_index >= len(candidates):
                raise SystemExit(
                    f"--input-index 범위를 벗어났습니다: {args.input_index} (0..{len(candidates)-1})"
                )
            input_path = candidates[args.input_index].resolve()
        else:
            # --input-auto 또는 아무 것도 안 준 경우: 단일 파일이면 자동 선택, 여러 개면 안내
            if len(candidates) == 1:
                input_path = candidates[0].resolve()
            else:
                listing = "\n".join([f"[{i}] {p.name}" for i, p in enumerate(candidates)])
                raise SystemExit(
                    "현재 폴더에 미디어 파일이 여러 개 있습니다. 하나를 선택해서 다시 실행하세요:\n"
                    f"{listing}\n\n"
                    "예) python transcribe.py --input-index 0 --language ko"
                )

    if not input_path.exists():
        raise SystemExit(f"입력 파일을 찾을 수 없습니다: {input_path}")

    out_dir = Path(args.output_dir).expanduser().resolve()
    out_dir.mkdir(parents=True, exist_ok=True)

    base_name = input_path.stem
    import whisper  # local import to ensure env vars above are applied before torch loads

    model = whisper.load_model(args.model, device=args.device)

    result = model.transcribe(
        str(input_path),
        language=args.language,
        task=args.task,
        verbose=False,
    )

    text = str(result.get("text", "")).strip()
    segments = result.get("segments") or []

    if "txt" in args.formats:
        _write_txt(out_dir / f"{base_name}.txt", text)
    if "json" in args.formats:
        _write_json(out_dir / f"{base_name}.json", result)
    if "srt" in args.formats:
        _write_srt(out_dir / f"{base_name}.srt", segments)
    if "vtt" in args.formats:
        _write_vtt(out_dir / f"{base_name}.vtt", segments)

    print(f"Done. outputs: {out_dir}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

