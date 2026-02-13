class AudioProcessor extends AudioWorkletProcessor {
    constructor() {
        super();
        this.buffer = [];
        this.targetSampleRate = 16000;
    }

    process(inputs, outputs, parameters) {
        const input = inputs[0];
        if (input && input.length > 0) {
            const channelData = input[0];
            // 메인 스레드로 Raw Float32Array 전송
            this.port.postMessage(channelData);
        }
        return true;
    }
}

registerProcessor('audio-processor', AudioProcessor);
