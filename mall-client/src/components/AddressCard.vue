<script setup>
import { Location, Delete, Edit } from '@element-plus/icons-vue'
import { ElTooltip } from 'element-plus'

const props = defineProps({
  address: {
    type: Object,
    required: true
  },
  selected: {
    type: Boolean,
    default: false
  },
  showDelete: {
    type: Boolean,
    default: false
  },
  showEdit: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['delete', 'edit', 'click'])

const getTooltipContent = (item) => {
  return item.region + item.address
}

const handleDelete = (e) => {
  e.stopPropagation()
  emit('delete', props.address)
}

const handleEdit = (e) => {
  e.stopPropagation()
  emit('edit', props.address)
}

const handleClick = () => {
  emit('click', props.address)
}
</script>

<template>
  <el-tooltip
    :content="getTooltipContent(address)"
    placement="top"
    :show-after="500"
  >
    <div :class="['address_card', { active: selected }]" @click="handleClick">
      <div v-if="showEdit || showDelete" class="action_icons">
        <div v-if="showEdit" class="edit_icon" @click="handleEdit">
          <el-icon :size="16"><Edit /></el-icon>
        </div>
        <div v-if="showDelete" class="delete_icon" @click="handleDelete">
          <el-icon :size="16"><Delete /></el-icon>
        </div>
      </div>
      <div class="card_content">
        <div class="location_icon_wrapper">
          <el-icon :size="20" :class="['location_icon', { active: selected }]">
            <Location />
          </el-icon>
        </div>
        <div class="address_info">
          <div class="address_region">{{ address.region }}</div>
          <div class="address_detail">{{ address.address }}</div>
          <div class="contact_info">
            <span class="name">{{ address.consignee }}</span>
            <span class="phone">{{ address.phone }}</span>
          </div>
        </div>
      </div>
    </div>
  </el-tooltip>
</template>

<style scoped>
.address_card {
  width: calc(33.333% - 11px);
  min-width: 280px;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fff;
  box-sizing: border-box;
  position: relative;
}

.address_card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.address_card.active {
  border-color: #ff6700;
  position: relative;
}

.action_icons {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s;
  z-index: 1;
}

.address_card:hover .action_icons {
  opacity: 1;
}

.edit_icon,
.delete_icon {
  color: #999;
  cursor: pointer;
}

.edit_icon:hover {
  color: #409EFF;
}

.delete_icon:hover {
  color: #ff6700;
}

.card_content {
  display: flex;
  gap: 12px;
}

.location_icon_wrapper {
  flex-shrink: 0;
  width: 24px;
  display: flex;
  justify-content: center;
  padding-top: 2px;
}

.location_icon {
  color: #999;
}

.location_icon.active {
  color: #ff6700;
}

.address_info {
  flex: 1;
  min-width: 0;
}

.address_region {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
  line-height: 1.5;
}

.address_detail {
  font-size: 15px;
  color: #333;
  font-weight: 500;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.5;
}

.contact_info {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}

.name {
  margin-right: 8px;
}
</style>
