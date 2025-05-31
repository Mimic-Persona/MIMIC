class CircularBuffer {
    constructor(capacity) {
        this.buffer = new Array(capacity);
        this.size = capacity;
        this.start = 0;
        this.end = 0;
        this.count = 0;
    }

    add(value) {
        if (this.count === this.size) {
            this.start = (this.start + 1) % this.size;
        } else {
            this.count++;
        }
        this.buffer[this.end] = value;
        this.end = (this.end + 1) % this.size;
    }

    getElements() {
        let elements = new Array(this.count);
        for (let i = 0; i < this.count; i++) {
            elements[i] = this.buffer[(this.start + i) % this.size];
        }
        return elements;
    }
}

module.exports = CircularBuffer;
