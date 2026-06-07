import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export interface CartItem {
  spuId: number
  skuId: number
  name: string
  price: number
  quantity: number
  image: string
  spec: string
}

export const useCartStore = defineStore('cart', () => {
  const cartItems = ref<CartItem[]>([])

  const totalCount = computed(() => {
    return cartItems.value.reduce((sum, item) => sum + item.quantity, 0)
  })

  const totalPrice = computed(() => {
    return cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
  })

  const addToCart = (item: CartItem) => {
    const existingIndex = cartItems.value.findIndex(
      i => i.skuId === item.skuId
    )

    if (existingIndex !== -1) {
      const existingItem = cartItems.value[existingIndex]
      if (existingItem) {
        existingItem.quantity += item.quantity
      }
    } else {
      cartItems.value.push(item)
    }
    
    saveToLocalStorage()
  }

  const updateQuantity = (skuId: number, quantity: number) => {
    const item = cartItems.value.find(i => i.skuId === skuId)
    if (item) {
      item.quantity = Math.max(1, quantity)
      saveToLocalStorage()
    }
  }

  const removeFromCart = (skuId: number) => {
    const index = cartItems.value.findIndex(i => i.skuId === skuId)
    if (index !== -1) {
      cartItems.value.splice(index, 1)
      saveToLocalStorage()
    }
  }

  const clearCart = () => {
    cartItems.value = []
    saveToLocalStorage()
  }

  const saveToLocalStorage = () => {
    localStorage.setItem('cart', JSON.stringify(cartItems.value))
  }

  const loadFromLocalStorage = () => {
    const saved = localStorage.getItem('cart')
    if (saved) {
      try {
        cartItems.value = JSON.parse(saved)
      } catch { /* ignore */ }
    }
  }

  loadFromLocalStorage()

  return { 
    cartItems, 
    totalCount, 
    totalPrice, 
    addToCart, 
    updateQuantity, 
    removeFromCart, 
    clearCart 
  }
})
