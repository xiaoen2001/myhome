// src/utils/avatar.js
export const DEFAULT_AVATAR = 'public/default-avatar.png' // 确保 public 目录下有该图片

export function getAvatarUrl(src) {
    if (!src || src === 'null' || src.trim() === '') {
        return DEFAULT_AVATAR
    }
    return src
}
